package com.portfolio.qiita_summary_notifier.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.portfolio.qiita_summary_notifier.service.QiitaNotificationService;

import lombok.RequiredArgsConstructor;


// 作ったはいいけど今じゃない気がする
// 簡単にテストできないし、一連の流れはテストクラスにある
@Controller 
@RequestMapping("/summary")
@RequiredArgsConstructor 
public class NotificationController {

    private final QiitaNotificationService qiitaNotificationService;

    @GetMapping("/test")
    public void exe() {
        qiitaNotificationService.execute(null, null);
    }
}
