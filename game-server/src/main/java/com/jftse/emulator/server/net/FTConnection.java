package com.jftse.emulator.server.net;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.entities.database.model.ServerType;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.net.Connection;
import com.jftse.server.core.protocol.IPacket;
import com.jftse.server.core.protocol.PacketRegistry;
import com.jftse.server.core.shared.MetricsService;
import com.jftse.server.core.shared.packets.SMSGServerTimeSync;
import com.jftse.server.core.shared.packets.enchant.CMSGEnchantRequest;
import com.jftse.server.core.shared.packets.gacha.CMSGOpenGacha;
import com.jftse.server.core.shared.packets.game.CMSGLoginData;
import com.jftse.server.core.shared.packets.guild.CMSGGuildJoin;
import com.jftse.server.core.shared.packets.home.CMSGClearHomeItems;
import com.jftse.server.core.shared.packets.home.CMSGPlaceHomeItems;
import com.jftse.server.core.shared.packets.inventory.CMSGCombineNowRecipe;
import com.jftse.server.core.shared.packets.inventory.CMSGInventoryItemTimeExpired;
import com.jftse.server.core.shared.packets.inventory.CMSGInventorySellItemCheck;
import com.jftse.server.core.shared.packets.matchplay.CMSGPlayerUseSkill;
import com.jftse.server.core.shared.packets.messenger.*;
import com.jftse.server.core.shared.packets.shop.CMSGShopBuy;
import com.jftse.server.core.thread.ThreadManager;
import com.jftse.server.core.util.Time;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
@Log4j2
public class FTConnection extends Connection<FTClient> {
    private String hwid;

    private ConcurrentLinkedQueue<IPacket> recvQueue = new ConcurrentLinkedQueue<>();

    private final static int MAX_PROCESSED_PACKETS_PER_UPDATE = 3;

    private final MetricsService metrics;

    private final static List<Integer> THREAD_HANDLED_PACKETS = List.of(
            CMSGLoginData.PACKET_ID,
            CMSGPlayerUseSkill.PACKET_ID,
            CMSGCombineNowRecipe.PACKET_ID,
            CMSGEnchantRequest.PACKET_ID,
            CMSGAddFriendApproval.PACKET_ID,
            CMSGSendParcel.PACKET_ID,
            CMSGAcceptParcel.PACKET_ID,
            CMSGDenyParcel.PACKET_ID,
            CMSGSendGift.PACKET_ID,
            CMSGInventorySellItemCheck.PACKET_ID,
            CMSGDeleteFriend.PACKET_ID,
            CMSGShopBuy.PACKET_ID,
            CMSGGuildJoin.PACKET_ID,
            CMSGInventoryItemTimeExpired.PACKET_ID,
            CMSGOpenGacha.PACKET_ID,
            CMSGPlaceHomeItems.PACKET_ID,
            CMSGClearHomeItems.PACKET_ID
    );

    private ScheduledFuture<?> timeSyncTask;
    private long lastTimeSyncSent = 0L;

    public FTConnection(final int decryptionKey, final int encryptionKey, final ServerType serverType) {
        super(decryptionKey, encryptionKey, serverType);
        this.metrics = ServiceManager.getInstance().getMetricsService();
    }

    public void queuePacket(IPacket packet) {
        recvQueue.add(packet);
    }

    private boolean isThreadedPacket(int packetId) {
        return THREAD_HANDLED_PACKETS.contains(packetId);
    }

    public boolean update(long diff) {
        final FTClient client = getClient();
        int processedPackets = 0;

        while (!getIsClosingConnection().get()) {
            if (processedPackets >= MAX_PROCESSED_PACKETS_PER_UPDATE || recvQueue.isEmpty()) {
                break;
            }

            final IPacket packet = recvQueue.poll();
            if (packet == null)
                continue;

            PacketHandler<FTConnection, IPacket> handler = PacketRegistry.getHandler(packet.getPacketId());
            if (handler != null) {
                if (isThreadedPacket(packet.getPacketId())) {
                    ThreadManager.getInstance().newTask(() -> runHandler(handler, packet));
                } else {
                    runHandler(handler, packet);
                }
            } else {
                log.warn("No handler for packet id: 0x{} ({})", Integer.toHexString(packet.getPacketId()), (int) packet.getPacketId());
            }

            processedPackets++;
        }

        if (getIsClosingConnection().get() && timeSyncTask != null) {
            timeSyncTask.cancel(false);
        }

        return !getIsClosingConnection().get();
    }

    private void runHandler(final PacketHandler<FTConnection, IPacket> handler, final IPacket packet) {
        final long updateStartTime = Time.getNSTime();
        try {
            handler.handle(this, packet);
        } catch (Exception e) {
            log.error("Error processing packet id: 0x{} ({})", Integer.toHexString(packet.getPacketId()), (int) packet.getPacketId(), e);
        }
        final long updateTime = Time.nanoToMillis(Time.getNSTimeDiff(updateStartTime, Time.getNSTime()));

        // track avg per packet id
        metrics.average("packet_process_time." + Integer.toHexString(packet.getPacketId()), updateTime, ServerType.GAME_SERVER);
    }

    public void timeSync() {
        final long nowMs = System.currentTimeMillis();
        SMSGServerTimeSync timeSyncPacket = SMSGServerTimeSync.builder().currentTime(Time.toFileTimeUTC(nowMs)).build();
        sendTCP(timeSyncPacket).addListener(future -> {
            if (future.isSuccess()) {
                lastTimeSyncSent = nowMs;
                nextTimeSync();
            }
        });
    }

    public void nextTimeSync() {
        if (timeSyncTask != null && !timeSyncTask.isDone()) {
            return;
        }

        timeSyncTask = ThreadManager.getInstance().schedule(this::timeSync, 30, TimeUnit.SECONDS);
    }
}
