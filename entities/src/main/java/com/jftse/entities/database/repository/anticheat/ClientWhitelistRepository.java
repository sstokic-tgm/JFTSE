package com.jftse.entities.database.repository.anticheat;

import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.anticheat.ClientWhitelist;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientWhitelistRepository extends JpaRepository<ClientWhitelist, Long> {
    Optional<ClientWhitelist> findByIpAndHwid(String ip, String hwid, Sort sort);
    List<ClientWhitelist> findAllByIpAndHwid(String ip, String hwid, Sort sort);
    Optional<ClientWhitelist> findByIpAndHwidAndFlaggedTrue(String ip, String hwid, Sort sort);
    List<ClientWhitelist> findAllByIpAndHwidAndFlaggedTrue(String ip, String hwid, Sort sort);
    Optional<ClientWhitelist> findByIpAndHwidAndAccount(String ip, String hwid, Account account, Sort sort);
    List<ClientWhitelist> findAllByIpAndHwidAndAccount(String ip, String hwid, Account account, Sort sort);
}