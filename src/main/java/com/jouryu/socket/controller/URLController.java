package com.jouryu.socket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by tomorrow on 18/11/9.
 */

@Controller
public class URLController {
    @RequestMapping("/")
    public String WebsocketChatClient(){
        return "/WebsocketClient";
    }
}
