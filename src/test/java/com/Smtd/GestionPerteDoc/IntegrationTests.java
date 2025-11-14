package com.Smtd.GestionPerteDoc;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    private String adminToken;

    @BeforeAll
    void setup() throws Exception {

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"assetoudiawara73@gmail.com\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        adminToken = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
        System.out.println(" Token admin récupéré : " + adminToken);
    }

    @Test
    void testDeclarationFlow() throws Exception {

        String declarationJson =
                "{\n" +
                        "  \"typeDocument\": { \"id\": 1 },\n" +
                        "  \"numeroDocument\": \"P1234567\",\n" +
                        "  \"datePerte\": \"2025-10-18\",\n" +
                        "  \"lieuPerte\": \"Bamako centre\",\n" +
                        "  \"circonstances\": \"Perdu dans un taxi\",\n" +
                        "  \"declarant\": {\n" +
                        "      \"nom\": \"DIOP\",\n" +
                        "      \"prenom\": \"Fatou\",\n" +
                        "      \"telephone\": \"+22370000003\",\n" +
                        "      \"email\": \"fatou.diop@test.com\",\n" +
                        "      \"adresse\": \"Rue Bamako 123\",\n" +
                        "      \"dateNaissance\": \"1990-05-01\",\n" +
                        "      \"lieuNaissance\": \"Bamako\"\n" +
                        "  }\n" +
                        "}";

        //  Création de la déclaration
        MvcResult createResult = mockMvc.perform(post("/api/declarations")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(declarationJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Déclaration créée avec succès et PDF envoyé par email"))
                .andReturn();

        Number idNumber = JsonPath.read(createResult.getResponse().getContentAsString(), "$.declaration.id");
        long declarationId = idNumber.longValue();

        System.out.println(" Déclaration créée ID = " + declarationId);

        //  Modifier statut
        mockMvc.perform(patch("/api/declarations/" + declarationId + "/statut")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("statut", "VALIDEE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("VALIDEE"));

        System.out.println(" Déclaration modifiée");

        //  Récupérer par ID
        mockMvc.perform(get("/api/declarations/" + declarationId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) declarationId));

        System.out.println(" Déclaration récupérée");

        //  Suppression logique
        mockMvc.perform(delete("/api/declarations/" + declarationId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Déclaration supprimée avec succès"));

        System.out.println(" Déclaration supprimée");

        //  Restauration
        mockMvc.perform(patch("/api/declarations/" + declarationId + "/restaurer")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Déclaration restaurée avec succès"));

        System.out.println(" Déclaration restaurée");

        //  Dashboard stats
        mockMvc.perform(get("/api/dashboard/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        System.out.println(" Stats dashboard OK");
    }
}
