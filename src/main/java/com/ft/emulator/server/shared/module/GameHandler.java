package com.ft.emulator.server.shared.module;

import com.ft.emulator.common.dao.GenericModelDao;
import com.ft.emulator.common.service.EntityManagerFactoryUtil;
import com.ft.emulator.server.database.model.challenge.Challenge;
import com.ft.emulator.server.database.model.character.CharacterPlayer;
import com.ft.emulator.server.game.matchplay.room.Room;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class GameHandler {

    private List<Client> clients;
    private List<Challenge> challenges;
    private List<Room> rooms;

    private GenericModelDao<Challenge> challengeDao;

    public GameHandler() {

	this.challengeDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), Challenge.class);

        this.clients = new ArrayList<>();
        this.challenges = getChallenges();
        this.rooms = new ArrayList<>();
    }

    private List<Challenge> getChallenges() {
	return challengeDao.getList();
    }

    public Challenge getChallenge(Long challengeId) {
        return challengeDao.find(challengeId);
    }

    public void addClient(Client client) {
        this.clients.add(client);
    }

    public List<CharacterPlayer> getCharacterPlayersInLobby() {

        return clients.stream()
                .filter(Client::isInLobby)
                .map(Client::getActiveCharacterPlayer)
                .collect(Collectors.toList());
    }

    public List<Client> getClientsInRoom(char roomId) {

        return clients.stream()
                .filter(c -> c.getActiveRoom() != null && c.getActiveRoom().getId() == roomId)
                .collect(Collectors.toList());
    }

    public List<Client> getClientsInLobby() {

        return clients.stream()
                .filter(Client::isInLobby)
                .collect(Collectors.toList());
    }

    public void removeClient(Client client) {
        this.clients.remove(client);
    }

    public List<Client> getClients() {
        return this.clients;
    }
}