package io.github.batetolast1.spring.demo.controllers;

import io.github.batetolast1.spring.demo.dto.*;
import io.github.batetolast1.spring.demo.model.domain.Advert;
import io.github.batetolast1.spring.demo.model.domain.User;
import io.github.batetolast1.spring.demo.model.repositories.AdvertRepository;
import io.github.batetolast1.spring.demo.model.repositories.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@Log4j2
public class UserAdvertsController {

    private final UserRepository userRepository;
    private final AdvertRepository advertRepository;

    @Autowired
    public UserAdvertsController(UserRepository userRepository, AdvertRepository advertRepository) {
        this.userRepository = userRepository;
        this.advertRepository = advertRepository;
    }

    @GetMapping(value = {"/user-adverts", "/user-adverts/{id:\\d+}"})
    public String getUserAdverts(@PathVariable(required = false) Long id, Principal principal, Model model) {
        String username = principal.getName();
        User loggedUser = userRepository.findByUsername(username);
        log.info("Logged user={}", loggedUser);

        User advertsOwner;
        if (id == null) {
            advertsOwner = loggedUser;
        } else {
            Optional<User> optionalUser = userRepository.findById(id);
            if (optionalUser.isEmpty()) {
                log.info("Adverts' owner not found, id={}", id);
                return "redirect:/";
            }
            advertsOwner = optionalUser.get();
        }

        ShowUserDTO userDTO = new ShowUserDTO();
        userDTO.setUsername(advertsOwner.getUsername());
        model.addAttribute("userDTO", userDTO);
        log.info("Adverts' owner={}", advertsOwner);

        List<Advert> ownersAdverts = advertRepository.findAllByUserOrderByPostedDesc(advertsOwner);
        log.info("Owner's adverts={}", ownersAdverts);

        List<ShowAdvertDTO> ownerAdvertDTOS = ownersAdverts.stream().map(advert -> {
            ShowAdvertDTO advertDTO = new ShowAdvertDTO();
            advertDTO.setId(advert.getId());
            advertDTO.setTitle(advert.getTitle());
            advertDTO.setDescription(advert.getDescription());
            advertDTO.setUserId(advert.getUser().getId());
            advertDTO.setUsername(advert.getUser().getUsername());
            advertDTO.setPosted(advert.getPosted());
            advertDTO.setCreatedByLoggedUser(loggedUser != null && loggedUser == advert.getUser());
            advertDTO.setObserved(loggedUser != null && loggedUser.getObservedAdverts().contains(advert));
            return advertDTO;
        }).collect(Collectors.toList());
        model.addAttribute("ownerAdvertDTOS", ownerAdvertDTOS);

        return "user-adverts-page";
    }

    @PostMapping("/delete-advert")
    public String deleteAdvert(DeleteAdvertDTO deleteAdvertDTO) {
        Optional<Advert> optionalAdvert = advertRepository.findById(deleteAdvertDTO.getAdvertId());

        if (optionalAdvert.isPresent()) {
            Advert advert = optionalAdvert.get();
            log.info("Advert to delete={}", advert);

            User loggedUser = userRepository.findByUsername(deleteAdvertDTO.getUsername());
            log.info("Logged user={}", loggedUser);

            if (loggedUser == advert.getUser()) {
                advertRepository.delete(advert);
                log.info("Advert deleted!");
            } else {
                log.info("Advert wasn't created by logged user, deleting failed!");
            }
        } else {
            log.info("Advert not found, id={}", deleteAdvertDTO.getAdvertId());
        }
        return "redirect:/user-adverts";
    }

    @GetMapping("/edit-advert")
    public String editAdvert(Long advertId, Principal principal, Model model) {
        String username = principal.getName();
        User loggedUser = userRepository.findByUsername(username);
        log.info("Logged user={}", loggedUser);

        Advert advert = advertRepository.getOne(advertId);
        log.info("Advert to edit={}", advert);

        if (loggedUser != advert.getUser()) {
            log.info("Advert wasn't created by logged user, can't edit!");
            return "redirect:/user-adverts";
        }

        model.addAttribute("editedAdvert", advert);
        return "edit-advert-form";
    }

    @PostMapping("/edit-advert")
    public String editAdvert(EditAdvertDTO editAdvertDTO) {
        User loggedUser = userRepository.findByUsername(editAdvertDTO.getUsername());
        log.info("Logged user={}", loggedUser);

        Advert advert = advertRepository.getOne(editAdvertDTO.getAdvertId());
        log.info("Advert to edit={}", advert);

        if (loggedUser == advert.getUser()) {
            advert.setTitle(editAdvertDTO.getTitle());
            advert.setDescription(editAdvertDTO.getDescription());
            log.info("Advert to update={}", advert);

            advertRepository.save(advert);
            log.info("Updated advert={}", advert);
        } else {
            log.info("Advert wasn't created by logged user, can't edit!");
        }
        return "redirect:/user-adverts";
    }

    @GetMapping("/observed-adverts")
    public String getObservedAdverts(Principal principal, Model model) {
        String username = principal.getName();
        User loggedUser = userRepository.findByUsername(username);
        log.info("Logged user={}", loggedUser);

        Set<Advert> observedAdverts = loggedUser.getObservedAdverts();
        model.addAttribute("observedAdverts", observedAdverts);
        log.info("Observed adverts={}", observedAdverts);
        return "observed-adverts-page";
    }

    @PostMapping("/observe-advert")
    public String observeAdvert(ObserveAdvertDTO observeAdvertDTO) {
        User loggedUser = userRepository.findByUsername(observeAdvertDTO.getUsername());
        log.info("Logged user={}", loggedUser);

        Advert advert = advertRepository.getOne(observeAdvertDTO.getAdvertId());
        log.info("Advert to observe={}", advert);

        if (advert.getUser() != loggedUser) {
            loggedUser.getObservedAdverts().add(advert);
            userRepository.save(loggedUser);
            log.info("Advert observed!");
        } else {
            log.info("Advert was created by logged user, can't be observed");
        }
        return "redirect:/observed-adverts";
    }

    @PostMapping("/unobserve-advert")
    public String unobserveAdvert(UnobserveAdvertDTO unobserveAdvertDTO) {
        User loggedUser = userRepository.findByUsername(unobserveAdvertDTO.getUsername());
        log.info("Logged user={}", loggedUser);

        Advert advert = advertRepository.getOne(unobserveAdvertDTO.getAdvertId());
        log.info("Advert to unobserve={}", advert);

        if (loggedUser.getObservedAdverts().remove(advert)) {
            userRepository.save(loggedUser);
            log.info("Advert unobserved!");
        } else {
            log.info("Advert was not observed, thus can't be unobserved");
        }

        return "redirect:/observed-adverts";
    }
}
