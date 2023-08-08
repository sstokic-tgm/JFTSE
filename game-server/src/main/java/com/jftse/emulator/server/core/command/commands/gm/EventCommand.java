package com.jftse.emulator.server.core.command.commands.gm;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.command.AbstractCommand;
import com.jftse.emulator.server.core.interaction.PlayerScriptable;
import com.jftse.emulator.server.core.interaction.PlayerScriptableImpl;
import com.jftse.emulator.server.core.life.event.GameEventRegistry;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.event.SGameEvent;
import com.jftse.server.core.service.GameEventService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class EventCommand extends AbstractCommand {
    private final GameEventService gameEventService;


    public EventCommand() {
        setDescription("Event configuration");

        gameEventService = ServiceManager.getInstance().getGameEventService();
    }

    private SGameEvent getGameEvent(Long id) throws ValidationException {
        Optional<SGameEvent> optGameEvent = gameEventService.findById(id);
        if (optGameEvent.isEmpty()) {
            throw new ValidationException("Event not found");
        }

        return optGameEvent.get();
    }

    private Date parseDate(String date) throws ValidationException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(date);
        } catch (Exception e) {
            if (StringUtils.isEmpty(date)) {
                return null;
            } else if (date.equalsIgnoreCase("now")) {
                return Calendar.getInstance().getTime();
            } else {
                throw new ValidationException("Invalid date format (use yyyy-MM-dd)");
            }
        }
    }

    @Override
    public void execute(FTConnection connection, List<String> params) {
        PlayerScriptableImpl playerScriptable = new PlayerScriptableImpl(connection.getClient());

        if (params.isEmpty()) {
            playerScriptable.sendChat("Server", "Use: -event <enable|disable|startDate|endDate>");
            return;
        }

        try {
            switch (params.get(0)) {
                case "enable": {
                    if (params.size() < 2) {
                        playerScriptable.sendChat("Server", "Use: -event enable <eventID>");
                        return;
                    }

                    SGameEvent gameEvent = getGameEvent(Long.valueOf(params.get(1)));
                    GameEventRegistry.getInstance().enableEvent(gameEvent.getId());

                    playerScriptable.sendChat("Server", "Event " + gameEvent.getName() + " (" + gameEvent.getId() + ") enabled");
                    break;
                }
                case "disable": {
                    if (params.size() < 2) {
                        playerScriptable.sendChat("Server", "Use: -event disable <eventID>");
                        return;
                    }

                    SGameEvent gameEvent = getGameEvent(Long.valueOf(params.get(1)));
                    GameEventRegistry.getInstance().disableEvent(gameEvent.getId());

                    playerScriptable.sendChat("Server", "Event " + gameEvent.getName() + " (" + gameEvent.getId() + ") disabled");
                    break;
                }
                case "startDate": {
                    if (params.size() < 3) {
                        playerScriptable.sendChat("Server", "Use: -event startDate <eventID> <date>");
                        return;
                    }

                    SGameEvent gameEvent = getGameEvent(Long.valueOf(params.get(1)));

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date startDate = parseDate(params.get(2));
                    if (startDate != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(startDate);
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);

                        gameEvent.setStartDate(cal.getTime());
                        gameEventService.save(gameEvent);

                        playerScriptable.sendChat("Server", "Event " + gameEvent.getName() + " (" + gameEvent.getId() + ") start date set to " + sdf.format(gameEvent.getStartDate()));
                    } else {
                        gameEvent.setStartDate(null);
                        gameEventService.save(gameEvent);

                        playerScriptable.sendChat("Server", "Event " + gameEvent.getName() + " (" + gameEvent.getId() + ") start date removed");
                    }

                    break;
                }
                case "endDate": {
                    if (params.size() < 3) {
                        playerScriptable.sendChat("Server", "Use: -event endDate <eventID> <date>");
                        return;
                    }

                    SGameEvent gameEvent = getGameEvent(Long.valueOf(params.get(1)));

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date endDate = parseDate(params.get(2));
                    if (endDate != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(endDate);
                        cal.set(Calendar.HOUR_OF_DAY, 23);
                        cal.set(Calendar.MINUTE, 59);
                        cal.set(Calendar.SECOND, 59);

                        gameEvent.setEndDate(cal.getTime());
                        gameEventService.save(gameEvent);

                        playerScriptable.sendChat("Server", "Event " + gameEvent.getName() + " (" + gameEvent.getId() + ") end date set to " + sdf.format(gameEvent.getEndDate()));
                    } else {
                        gameEvent.setEndDate(null);
                        gameEventService.save(gameEvent);

                        playerScriptable.sendChat("Server", "Event " + gameEvent.getName() + " (" + gameEvent.getId() + ") end date removed");
                    }

                    break;
                }
                default:
                    playerScriptable.sendChat("Server", "Use: -event <enable|disable|startDate|endDate>");
                    break;
            }
        } catch (ValidationException e) {
            playerScriptable.sendChat("Server", e.getMessage());
        }
    }
}
