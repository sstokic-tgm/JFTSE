package com.ft.emulator.server.shared.module;

import com.ft.emulator.server.database.model.account.Account;
import com.ft.emulator.server.database.model.character.CharacterPlayer;
import com.ft.emulator.server.game.matchplay.room.Room;
import com.ft.emulator.server.game.server.PacketStream;
import com.ft.emulator.server.game.singleplay.challenge.ChallengeGame;
import com.ft.emulator.server.game.singleplay.tutorial.TutorialGame;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.Socket;

@Getter
@Setter
public class Client {

    private Socket clientSocket;
    private PacketStream packetStream;

    private Account account;
    private CharacterPlayer activeCharacterPlayer;

    private ChallengeGame activeChallengeGame;
    private TutorialGame activeTutorialGame;

    private Room activeRoom;
    private boolean inLobby;

    public Client(Socket clientSocket) throws IOException {

        clientSocket.setTcpNoDelay(true);
        this.clientSocket = clientSocket;
        this.packetStream = new PacketStream(this.clientSocket, new byte[4], new byte[4]);
    }
}