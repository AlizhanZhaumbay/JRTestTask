package com.game.service;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayersRepository;
import com.game.util.PlayerNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.openmbean.OpenDataException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class PlayersService {
    private final PlayersRepository playersRepository;

    @Autowired
    public PlayersService(PlayersRepository playersRepository) {
        this.playersRepository = playersRepository;
    }

    public List<Player> findPlayers(Integer pageNumber, Integer pageSize, String playerOrder, String name, String title,
                                    Race race, Profession profession, Boolean banned, Long before,
                                    Long after, Integer minExperience, Integer maxExperience,
                                    Integer minLevel, Integer maxLevel) {

        Specification<Player> specification = getPlayerSpecification(pageNumber, pageSize, playerOrder, name, title,
                race, profession, banned, before, after, minExperience, maxExperience, minLevel, maxLevel);

        return playersRepository.findAll(specification, PageRequest.of(pageNumber, pageSize, Sort.by(playerOrder))).getContent();
    }

    public Specification<Player> getPlayerSpecification(Integer pageNumber, Integer pageSize, String playerOrder, String name,
                                                        String title, Race race, Profession profession,
                                                        Boolean banned, Long before, Long after, Integer minExperience, Integer maxExperience,
                                                        Integer minLevel, Integer maxLevel) {
        return new Specification<Player>() {
            @Override
            public Predicate toPredicate(Root<Player> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (name != null) {
                    predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
                }
                if (title != null) {
                    predicates.add(criteriaBuilder.like(root.get("title"), "%" + title + "%"));
                }
                if (race != null) {
                    predicates.add(criteriaBuilder.equal(root.get("race"), race));
                }
                if (profession != null) {
                    predicates.add(criteriaBuilder.equal(root.get("profession"), profession));
                }
                if (banned != null) {
                    predicates.add(criteriaBuilder.equal(root.get("banned"), banned));
                }
                if (before != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.<Date>get("birthday"), new Date(before)));
                }
                if (after != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.<Date>get("birthday"), new Date(after)));
                }
                if (minExperience != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.<Integer>get("experience"), minExperience));
                }
                if (maxExperience != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.<Integer>get("experience"), maxExperience));
                }
                if (minLevel != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("level"), minLevel));
                }
                if (maxLevel != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("level"), maxLevel));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }

    public Player findPlayer(Long id) {
        if (id == null || id == 0) throw new IllegalArgumentException();
        Optional<Player> optionalPlayer = playersRepository.findById(id);
        return optionalPlayer.orElseThrow(PlayerNotFoundException::new);
    }

    public long count(Integer pageNumber, Integer pageSize, String playerOrder, String name, String title, Race race, Profession profession, Boolean banned, Long before, Long after, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel) {
        Specification<Player> specification = getPlayerSpecification(pageNumber, pageSize, playerOrder, name, title,
                race, profession, banned, before, after, minExperience, maxExperience, minLevel, maxLevel);
        return playersRepository.count(specification);
    }

    @Transactional
    public void deletePlayerById(Long id) {
        if (Objects.isNull(id) || id == 0) throw new IllegalArgumentException();
        Optional<Player> optionalPlayer = playersRepository.findById(id);
        if (!optionalPlayer.isPresent()) throw new PlayerNotFoundException();
        playersRepository.deleteById(optionalPlayer.get().getId());
    }


    @Transactional
    public Player save(Player player) {
        playersRepository.save(player);
        return player;
    }
}
