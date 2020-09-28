package io.github.batetolast1.spring.demo.controllers;

import io.github.batetolast1.spring.demo.dto.ShowAdvertDTO;
import io.github.batetolast1.spring.demo.model.domain.Advert;
import io.github.batetolast1.spring.demo.model.domain.User;
import io.github.batetolast1.spring.demo.model.repositories.AdvertRepository;
import io.github.batetolast1.spring.demo.model.repositories.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
@Log4j2
public class HomePageController {

    private final UserRepository userRepository;
    private final AdvertRepository advertRepository;

    @Autowired
    public HomePageController(UserRepository userRepository, AdvertRepository advertRepository) {
        this.userRepository = userRepository;
        this.advertRepository = advertRepository;
    }

    @GetMapping
    public String prepareHomePage(Principal principal, Model model) {
        User loggedUser = (principal != null) ? userRepository.findByUsername(principal.getName()) : null;
        log.info("Logged user={}", loggedUser);

        List<Advert> adverts = (principal != null) ? advertRepository.findAllByOrderByPostedDesc() : advertRepository.findFirst10ByOrderByPostedDesc();
        log.info("All adverts={}", adverts);

        List<ShowAdvertDTO> advertDTOS = adverts.stream().map(advert -> {
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
        model.addAttribute("advertDTOS", advertDTOS);

        return "home-page";
    }
}
