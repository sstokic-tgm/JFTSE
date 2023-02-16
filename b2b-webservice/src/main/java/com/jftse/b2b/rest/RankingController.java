package com.jftse.b2b.rest;

import com.jftse.b2b.dto.PlayerDto;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.repository.player.PlayerRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class RankingController {
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PlayerRepository playerRepository;

    @GetMapping("ranking")
    public List<PlayerDto> getRanking(
            @RequestParam(value = "page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("gamemode") String gameMode) {

        String gameModeRP;
        if (gameMode.equalsIgnoreCase("basic"))
            gameModeRP = "playerStatistic.basicRP";
        else if (gameMode.equalsIgnoreCase("battle"))
            gameModeRP = "playerStatistic.battleRP";
        else
            gameModeRP = "playerStatistic.guardianRP";

        List<Player> allPlayers = playerRepository.findAllByAlreadyCreatedTrue(
                PageRequest.of(
                        page.orElse(1) == 1 ? 0 : page.orElse(1) - 1, 
                        size.orElse(10), 
                        Sort.by(gameModeRP).descending().and(Sort.by("created"))
                )).getContent();

        List<PlayerDto> result = new ArrayList<>();
        for (int i = 0; i < allPlayers.size(); i++) {
            int ranking = (page.orElse(1) == 1 ? 0 : (page.orElse(1) * 10) - 10) + 1 + i;
            PlayerDto playerDto = modelMapper.map(allPlayers.get(i), PlayerDto.class);
            playerDto.setRank(ranking);

            result.add(playerDto);
        }

        return result;
    }
}
