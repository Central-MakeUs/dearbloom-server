package kr.co.dearbloom.global.dto.response.exception;

import kr.co.dearbloom.domain.artist.controller.ArtistController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Region enum 에 없는 값이 오면 500 이 아니라 400 으로 응답하는지 검증.
 * 컨트롤러 진입 전 본문 파싱 단계에서 실패하므로 인증 없이 standalone 으로 확인 가능.
 */
@SpringBootTest
class InvalidEnumRequestTest {
    @Autowired ArtistController artistController;
    @Autowired GlobalExceptionHandler globalExceptionHandler;

    @Test
    void 존재하지_않는_region_값은_400_을_반환한다() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(artistController)
                .setControllerAdvice(globalExceptionHandler)
                .build();

        mockMvc.perform(put("/api/artists/me/regions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"regions\":[\"SEOUUL\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("REQUEST-400"));
    }
}
