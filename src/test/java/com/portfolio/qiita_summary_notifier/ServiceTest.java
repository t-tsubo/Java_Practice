package com.portfolio.qiita_summary_notifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.genai.Client;
import com.google.genai.gaos.models.interactions.Content;
import com.google.genai.gaos.models.interactions.CreateModelInteraction;
import com.google.genai.gaos.models.interactions.CreateModelInteractionResponseFormat;
import com.google.genai.gaos.models.interactions.Interaction;
import com.google.genai.gaos.models.interactions.InteractionsInput;
import com.google.genai.gaos.models.interactions.Model;
import com.google.genai.gaos.models.interactions.ModelOutputStep;
import com.google.genai.gaos.models.interactions.ResponseFormat;
import com.google.genai.gaos.models.interactions.Step;
import com.google.genai.gaos.models.interactions.TextContent;
import com.google.genai.gaos.models.interactions.TextResponseFormat;
import com.google.genai.gaos.models.interactions.TextResponseFormatMimeType;
import com.google.genai.gaos.models.operations.CreateInteractionRequestBody;
import com.portfolio.qiita_summary_notifier.entity.NotificationSetting;
import com.portfolio.qiita_summary_notifier.service.QiitaNotificationService;

import lombok.extern.slf4j.Slf4j;


@Slf4j 
@SpringBootTest 
public class ServiceTest {

    @Value("${discord.webhook.token}")
    String discordWebhookToken;

    @Value("${gemini.api.token}")
    String geminiApiToken;

    @Autowired 
    QiitaNotificationService qiitaNotificationService;



    @Test 
    void singleSettingNotificationTest() {
        // settingオブジェクトを作成
        NotificationSetting testSetting = new NotificationSetting();
        // 必要なデータだけsetterで入れていく
        testSetting.setId(1);
        testSetting.setIncludeTags("Java, spring");
        testSetting.setExcludeTags("ポエム , , MCP");
        testSetting.setWebhookUrl(discordWebhookToken);

        // settingをexecuteForSetting()に渡す
        qiitaNotificationService.deliverArticles(testSetting);

    }

    @Test 
    void geminiAPIを飛ばすテストのながれ() {
        /**テストの流れ
         * QiitaとDiscordは実際に動かす、Geminiは動かさない場合
         * articleProvider.fetchArticles():     記事を取得(QiitaAPI)
         * ここで分岐を入れる  
         * articleIdをもとにif文で条件分岐
         * キャッシュに存在している場合、
         *      キャッシュから要約済みのjsonを文字列に格納
         * キャッシュが存在していない場合、
         *      summarizer.fetchSummaryOfArticle()を走らせる
         *      実際にgeminiに問い合わせてjsonを取得し、その内容を文字列に格納
         * 文字列に格納された情報をDtoで一度整形？よくわからなくなってきた
         * GeminiResponseDtoを使っていないので、どうやって使っていたかを確認して思い出す
         * 
         */
    }

    @Test 
    void gemini構造化サンプル() {

        Client client = Client.builder()
                .apiKey(geminiApiToken)
                .build();

        Map<String, Object> ingredientProps = new HashMap<>();
        Map<String, Object> nameProp = new HashMap<>();
        nameProp.put("type", "string");
        nameProp.put("description", "Name of the ingredient.");
        ingredientProps.put("name", nameProp);

        Map<String, Object> quantityProp = new HashMap<>();
        quantityProp.put("type", "string");
        quantityProp.put("description", "Quantity of the ingredient, including units.");
        ingredientProps.put("quantity", quantityProp);

        Map<String, Object> ingredientItemSchema = new HashMap<>();
        ingredientItemSchema.put("type", "object");
        ingredientItemSchema.put("properties", ingredientProps);
        ingredientItemSchema.put("required", Arrays.asList("name", "quantity"));

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> recipeNameProp = new HashMap<>();
        recipeNameProp.put("type", "string");
        recipeNameProp.put("description", "The name of the recipe.");
        properties.put("recipe_name", recipeNameProp);

        Map<String, Object> prepTimeProp = new HashMap<>();
        prepTimeProp.put("type", "integer");
        prepTimeProp.put("description", "Optional time in minutes to prepare the recipe.");
        properties.put("prep_time_minutes", prepTimeProp);

        Map<String, Object> ingredientsProp = new HashMap<>();
        ingredientsProp.put("type", "array");
        ingredientsProp.put("items", ingredientItemSchema);
        properties.put("ingredients", ingredientsProp);

        Map<String, Object> instructionsProp = new HashMap<>();
        instructionsProp.put("type", "array");
        Map<String, Object> stringItem = new HashMap<>();
        stringItem.put("type", "string");
        instructionsProp.put("items", stringItem);
        properties.put("instructions", instructionsProp);

        Map<String, Object> recipeJsonSchema = new HashMap<>();
        recipeJsonSchema.put("type", "object");
        recipeJsonSchema.put("properties", properties);
        recipeJsonSchema.put("required", Arrays.asList("recipe_name", "ingredients", "instructions"));

        String prompt =
            "Please extract the recipe from the following text.\n"
                + "The user wants to make delicious chocolate chip cookies.\n"
                + "They need 2 and 1/4 cups of all-purpose flour, 1 teaspoon of baking soda,\n"
                + "1 teaspoon of salt, 1 cup of unsalted butter (softened), 3/4 cup of granulated sugar,\n"
                + "3/4 cup of packed brown sugar, 1 teaspoon of vanilla extract, and 2 large eggs.\n"
                + "For the best part, they'll need 2 cups of semisweet chocolate chips.\n"
                + "First, preheat the oven to 375°F (190°C). Then, in a small bowl, whisk together the flour,\n"
                + "baking soda, and salt. In a large bowl, cream together the butter, granulated sugar, and brown sugar\n"
                + "until light and fluffy. Beat in the vanilla and eggs, one at a time. Gradually beat in the dry\n"
                + "ingredients until just combined. Finally, stir in the chocolate chips. Drop by rounded tablespoons\n"
                + "onto ungreased baking sheets and bake for 9 to 11 minutes.";

        CreateModelInteractionResponseFormat format =
            CreateModelInteractionResponseFormat.of(
                ResponseFormat.of(
                    TextResponseFormat.builder()
                        .mimeType(TextResponseFormatMimeType.APPLICATION_JSON)
                        .schema(recipeJsonSchema)
                        .build()));

        CreateModelInteraction params =
            CreateModelInteraction.builder()
                .model(Model.of("gemini-3.5-flash-lite"))
                .input(InteractionsInput.of(prompt))
                .responseFormat(format)
                .build();

        System.out.println("getリクエストを送信");
        Interaction interaction =
            client.interactions.create(CreateInteractionRequestBody.of(params)).interaction().get();
        log.debug("interactionの中身: {}", interaction);

                StringBuilder output = new StringBuilder();

        for (Step step : interaction.steps().orElse(List.of())) {
            if (step instanceof ModelOutputStep modelOutputStep) {
                for (Content content : modelOutputStep.content().orElse(List.of())) {
                    if (content instanceof TextContent textContent) {
                        textContent.text().ifPresent(output::append);
                    }
                }
            }
        }

        System.out.println(output.length() > 0 ? output.toString() : "geminiエラー");
        
    }
}
