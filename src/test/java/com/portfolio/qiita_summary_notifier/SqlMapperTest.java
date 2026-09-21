package com.portfolio.qiita_summary_notifier;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.portfolio.qiita_summary_notifier.entity.NotificationSetting;
import com.portfolio.qiita_summary_notifier.repository.NotificationSettingMapper;
import com.portfolio.qiita_summary_notifier.utility.Utility;

@SpringBootTest 
class SqlMapperTest {

    @Autowired 
    NotificationSettingMapper mapper;

    @Test 
    void testSelectActiveSettings() {
        List<NotificationSetting> activeSettings = mapper.selectActiveSettings();

        for (NotificationSetting setting : activeSettings) {
            Utility.writeUtf8Texts("logs/SQLTest.txt", setting.getSearchKeywords());
        }
    }
}
