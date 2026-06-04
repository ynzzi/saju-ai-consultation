package com.portfolio.saju.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

    @GetMapping("/view/signup")
    public String signup() {
        return "view/signup";
    }

    @GetMapping("/view/login")
    public String login() {
        return "view/login";
    }

    @GetMapping("/view/profiles")
    public String profiles() {
        return "view/profiles";
    }

    @GetMapping("/view/profiles/new")
    public String newProfile() {
        return "view/profile-new";
    }

    @GetMapping("/view/profiles/{profileId}")
    public String profileDetail(@PathVariable Long profileId) {
        return "view/profile-detail";
    }
}
