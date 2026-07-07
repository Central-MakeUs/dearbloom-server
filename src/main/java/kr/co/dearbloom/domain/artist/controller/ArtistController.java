package kr.co.dearbloom.domain.artist.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Artist", description = "작가 API")
@Hidden // ToDo
public class ArtistController {
}
