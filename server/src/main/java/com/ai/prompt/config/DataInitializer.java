package com.ai.prompt.config;

import com.ai.prompt.entity.SubscriptionPlan;
import com.ai.prompt.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 数据初始化器
 *
 * <p>应用启动时初始化默认套餐数据。</p>
 *
 * @author 马可行
 * @since 1.2.0
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SubscriptionPlanRepository planRepository;

    @Override
    public void run(String... args) {
        // 初始化免费版
        if (planRepository.findByCode("FREE").isEmpty()) {
            SubscriptionPlan free = new SubscriptionPlan();
            free.setCode("FREE");
            free.setName("免费版");
            free.setPriceMonthly(BigDecimal.ZERO);
            free.setPriceYearly(BigDecimal.ZERO);
            free.setGenerateLimit(20);
            free.setFavoriteLimit(50);
            free.setFeatures("basic_templates");
            free.setSortOrder(1);
            planRepository.save(free);
        }

        // 初始化专业版
        if (planRepository.findByCode("PRO").isEmpty()) {
            SubscriptionPlan pro = new SubscriptionPlan();
            pro.setCode("PRO");
            pro.setName("专业版");
            pro.setPriceMonthly(new BigDecimal("29.00"));
            pro.setPriceYearly(new BigDecimal("279.00"));
            pro.setGenerateLimit(-1);
            pro.setFavoriteLimit(-1);
            pro.setFeatures("all_templates,export,priority_support");
            pro.setSortOrder(2);
            planRepository.save(pro);
        }

        // 初始化团队版
        if (planRepository.findByCode("TEAM").isEmpty()) {
            SubscriptionPlan team = new SubscriptionPlan();
            team.setCode("TEAM");
            team.setName("团队版");
            team.setPriceMonthly(new BigDecimal("99.00"));
            team.setPriceYearly(new BigDecimal("899.00"));
            team.setGenerateLimit(-1);
            team.setFavoriteLimit(-1);
            team.setFeatures("all_templates,custom_templates,export,api_access,team_collaboration,dedicated_manager");
            team.setSortOrder(3);
            planRepository.save(team);
        }
    }
}
