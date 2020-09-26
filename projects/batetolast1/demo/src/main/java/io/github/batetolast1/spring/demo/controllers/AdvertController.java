package io.github.batetolast1.spring.demo.controllers;

import io.github.batetolast1.spring.demo.model.domain.Advert;
import io.github.batetolast1.spring.demo.model.domain.User;
import io.github.batetolast1.spring.demo.model.repositories.AdvertRepository;
import io.github.batetolast1.spring.demo.model.repositories.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@Log4j2
public class AdvertController {

    private final UserRepository userRepository;
    private final AdvertRepository advertRepository;

    @Autowired
    public AdvertController(UserRepository userRepository, AdvertRepository advertRepository) {
        this.userRepository = userRepository;
        this.advertRepository = advertRepository;
    }

    @PostMapping("/add-advert")
    public String addAdvert(String title,
                            String description,
                            Principal principal) { // obiekt klasy Principal reprezentuje ogólną informację o zalogowanym użytkowniku
        String username = principal.getName(); // zwraca nazwę zalogowanego użytkownika (pole username w klasie User)
        User user = userRepository.findByUsername(username);
        Advert advert = Advert.builder()
                .title(title)
                .description(description)
                .posted(LocalDateTime.now())
                .user(user)
                .build();
        log.info("Advert to add: {}", advert);

        advertRepository.save(advert);
        log.info("Added advert: {}", advert);

        return "redirect:/"; // po wykonaniu logiki zwrócona zostanie informacja o przekierowaniu (przeglądarka zrobi to automatycznie)
        // obsługę oddajemy kontrolerowi HomePageController, która przygotuje model
    }
}