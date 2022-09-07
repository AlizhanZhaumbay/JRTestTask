package com.game.controller;

import com.game.dto.PlayerDTO;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayersService;


import com.game.util.PlayerNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/rest/players")
public class PlayersController {
    private final PlayersService playersService;

    @Autowired
    public PlayersController(PlayersService playersService) {
        this.playersService = playersService;
    }


    @GetMapping
    public List<Player> getAllPlayers(@RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                                      @RequestParam(name = "pageSize", required = false, defaultValue = "3") Integer pageSize,
                                      @RequestParam(name = "order", required = false, defaultValue = "id") String playerOrder,
                                      @RequestParam(name = "name", required = false) String name,
                                      @RequestParam(name = "title", required = false) String title,
                                      @RequestParam(name = "race", required = false) Race race,
                                      @RequestParam(name = "profession", required = false) Profession profession,
                                      @RequestParam(name = "banned", required = false) Boolean banned,
                                      @RequestParam(name = "before", required = false) Long before,
                                      @RequestParam(name = "after", required = false) Long after,
                                      @RequestParam(name = "minExperience", required = false) Integer minExperience,
                                      @RequestParam(name = "maxExperience", required = false) Integer maxExperience,
                                      @RequestParam(name = "minLevel", required = false) Integer minLevel,
                                      @RequestParam(name = "maxLevel", required = false) Integer maxLevel) {
        return playersService.findPlayers(pageNumber, pageSize, playerOrder.toLowerCase(), name, title,
                race, profession, banned, before, after, minExperience, maxExperience, minLevel, maxLevel);
    }

    @GetMapping("/{id}")
    public Player getPlayer(@PathVariable("id") long id) {
        return playersService.findPlayer(id);
    }

    @GetMapping("/count")
    public long getCountOfPlayers(@RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                                  @RequestParam(name = "pageSize", required = false, defaultValue = "3") Integer pageSize,
                                  @RequestParam(name = "order", required = false, defaultValue = "id") String playerOrder,
                                  @RequestParam(name = "name", required = false) String name,
                                  @RequestParam(name = "title", required = false) String title,
                                  @RequestParam(name = "race", required = false) Race race,
                                  @RequestParam(name = "profession", required = false) Profession profession,
                                  @RequestParam(name = "banned", required = false) Boolean banned,
                                  @RequestParam(name = "before", required = false) Long before,
                                  @RequestParam(name = "after", required = false) Long after,
                                  @RequestParam(name = "minExperience", required = false) Integer minExperience,
                                  @RequestParam(name = "maxExperience", required = false) Integer maxExperience,
                                  @RequestParam(name = "minLevel", required = false) Integer minLevel,
                                  @RequestParam(name = "maxLevel", required = false) Integer maxLevel) {
        return playersService.count(pageNumber, pageSize, playerOrder.toLowerCase(), name, title,
                race, profession, banned, before, after, minExperience, maxExperience, minLevel, maxLevel);
    }

    @DeleteMapping("/{id}")
    public void deletePlayer(@PathVariable("id") Long id) {
        playersService.deletePlayerById(id);
    }

    @PostMapping
    public Player createPlayer(@RequestBody PlayerDTO playerDTO) {
        return playersService.save(convertToPlayer(playerDTO));
    }


    @PostMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Player> updatePlayer(@PathVariable("id") Long id, @RequestBody PlayerDTO playerDTO){

        Player player = playersService.findPlayer(id);
        if (playerDTO.getName() != null){
            if(playerDTO.getName().length() > 12)
                throw new IllegalArgumentException();
            player.setName(playerDTO.getName());
        }
        if (playerDTO.getTitle() != null){
            if(playerDTO.getTitle().length() > 30)
                throw new IllegalArgumentException();
            player.setTitle(playerDTO.getTitle());
        }
        if (playerDTO.getRace() != null)
            player.setRace(playerDTO.getRace());
        if (playerDTO.getProfession() != null)
            player.setProfession(playerDTO.getProfession());
        if (playerDTO.getExperience() != null){
            if(playerDTO.getExperience() > 10000000 || playerDTO.getExperience() < 0)
                throw new IllegalArgumentException();
            setExperience(playerDTO,player);
        }
        if(playerDTO.getBirthday() != null){
            Date birthday = playerDTO.getBirthday();
            if(birthday.getTime() < 0 || birthday.after(new Date(3000,Calendar.DECEMBER,31,23,59,59)))
                throw new IllegalArgumentException();
            player.setBirthday(birthday);
        }
        if(playerDTO.getBanned() != null){
            player.setBanned(playerDTO.getBanned());
        }
        return new ResponseEntity<>(playersService.save(player),HttpStatus.OK);
    }


    @ExceptionHandler
    private ResponseEntity<HttpStatus> handlePlayerIllegalArgumentException(IllegalArgumentException e){
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<HttpStatus> handlePlayerIllegalArgumentException(PlayerNotFoundException e){
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    private void setExperience(PlayerDTO playerDTO, Player player){
        player.setExperience(playerDTO.getExperience());
        int level = (int) (Math.sqrt(2500 + (200 * playerDTO.getExperience())) - 50) / 100;
        int untilNextLevel = 50 * (level + 1) * (level + 2) - playerDTO.getExperience();
        player.setLevel(level);
        player.setUntilNextLevel(untilNextLevel);
    }


    private Player convertToPlayer(PlayerDTO playerDTO) {
        Player player = new Player();
        if (playerDTO.getName() == null || playerDTO.getName().length() > 12)
            throw new IllegalArgumentException();
        if (playerDTO.getTitle() == null || playerDTO.getTitle().length() > 30)
            throw new IllegalArgumentException();
        if (playerDTO.getRace() == null || playerDTO.getProfession() == null)
            throw new IllegalArgumentException();
        if (playerDTO.getBirthday() == null ||
                playerDTO.getBirthday().after(new Date(3000, Calendar.DECEMBER, 31)))
        {
            throw new IllegalArgumentException();
        }
        if (playerDTO.getExperience() == null || playerDTO.getExperience() < 0 || playerDTO.getExperience() > 1000000)
            throw new IllegalArgumentException();
        player.setBanned(playerDTO.getBanned() != null);

        player.setName(playerDTO.getName());
        player.setTitle(playerDTO.getTitle());
        player.setRace(playerDTO.getRace());
        player.setProfession(playerDTO.getProfession());
        player.setBirthday(playerDTO.getBirthday());

        setExperience(playerDTO,player);
        playersService.save(player);
        return player;
    }

}

