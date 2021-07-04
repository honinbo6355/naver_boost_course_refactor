package refactor.naver.reserve.reserveweb_refactor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ViewController {

    @GetMapping("mainpage")
    public ModelAndView mainPage() {
        return new ModelAndView("page/mainpage");
    }
}