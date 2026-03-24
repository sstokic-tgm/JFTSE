package com.jftse.entities.database.repository.guild;

import com.jftse.entities.database.model.guild.Guild;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GuildRepository extends JpaRepository<Guild, Long> {
    List<Guild> findAllByName(String name);

    @Query("SELECT DISTINCT g FROM Guild g LEFT JOIN FETCH g.memberList gm LEFT JOIN FETCH gm.player p WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Guild> findAllByNameContainingIgnoreCase(String name);

    @Query("SELECT g.id FROM Guild g WHERE g.id > :id ORDER BY g.id ASC")
    List<Long> findIdsByIdGreaterThanOrderByIdAsc(Long id, Pageable pageable);

    List<Guild> findAllByOrderByLeaguePointsDescIdAsc(Pageable pageable);

    @Query("""
              SELECT DISTINCT g
              FROM Guild g
              LEFT JOIN FETCH g.memberList gm
              LEFT JOIN FETCH gm.player p
              WHERE g.id IN :ids
              ORDER BY g.id ASC
            """)
    List<Guild> findWithMembersByIdIn(List<Long> ids);

    @Query("SELECT DISTINCT g FROM Guild g LEFT JOIN FETCH g.memberList gm LEFT JOIN FETCH gm.player p LEFT JOIN FETCH p.account acc WHERE g.id = :id")
    Optional<Guild> findWithMembersById(Long id);
}
