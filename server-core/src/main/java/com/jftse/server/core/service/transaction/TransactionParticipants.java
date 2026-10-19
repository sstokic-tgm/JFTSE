package com.jftse.server.core.service.transaction;

import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.repository.transaction.TransactionAccountRepository;
import com.jftse.entities.database.repository.transaction.TransactionPlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import static com.jftse.server.core.service.transaction.TransactionException.Reason.INVALID_REQUEST;

@Component
@RequiredArgsConstructor
public class TransactionParticipants {
    private final TransactionAccountRepository accountRepository;
    private final TransactionPlayerRepository playerRepository;
    private final EntityManager entityManager;

    public Participants lock(Long accountId, Long playerId, Player counterparty) {
        Long counterpartyAccountId = counterparty.getAccount().getId();
        TreeSet<Long> accountIds = new TreeSet<>();
        accountIds.add(accountId);
        accountIds.add(counterpartyAccountId);

        Map<Long, Account> accounts = new HashMap<>();
        for (Long id : accountIds) {
            Account account = accountRepository.findById(id)
                    .orElseThrow(() -> new TransactionException(INVALID_REQUEST));
            entityManager.refresh(account, LockModeType.PESSIMISTIC_WRITE);
            accounts.put(id, account);
        }

        // Consistent ordering also covers transfers between two characters on one account.
        Map<Long, Long> playerAccounts = new TreeMap<>();
        playerAccounts.put(counterparty.getId(), counterpartyAccountId);
        playerAccounts.put(playerId, accountId);

        Map<Long, Player> players = new HashMap<>();
        for (Map.Entry<Long, Long> entry : playerAccounts.entrySet()) {
            Player player = playerRepository.findForTransaction(entry.getKey(), entry.getValue())
                    .orElseThrow(() -> new TransactionException(INVALID_REQUEST));
            entityManager.refresh(player, LockModeType.PESSIMISTIC_WRITE);
            players.put(entry.getKey(), player);
        }

        return new Participants(accounts.get(accountId), players.get(playerId),
                accounts.get(counterpartyAccountId), players.get(counterparty.getId()));
    }

    public record Participants(Account account, Player player, Account counterpartyAccount, Player counterparty) {
    }
}
