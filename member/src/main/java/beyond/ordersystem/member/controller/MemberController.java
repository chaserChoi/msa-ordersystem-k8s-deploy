package beyond.ordersystem.member.controller;

import beyond.ordersystem.common.auth.JwtTokenProvider;
import beyond.ordersystem.common.dto.CommonDto;
import beyond.ordersystem.member.domain.Member;
import beyond.ordersystem.member.dto.*;
import beyond.ordersystem.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/create")
    public ResponseEntity<?> memberCreate(@RequestBody @Valid MemberCreateDto dto) {
        Long id = memberService.save(dto);
        return new ResponseEntity<>(new CommonDto(id, HttpStatus.CREATED.value(), "회원가입 성공"), HttpStatus.CREATED);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> memberDoLogin(@RequestBody @Valid LoginReqDto dto) {
        Member member = memberService.doLogin(dto);
        // at 토큰 생성
        String accessToken = jwtTokenProvider.createAtToken(member);
        // rt 토큰 생성
        String refreshToken = jwtTokenProvider.createAtToken(member);

        /*LoginResDto loginResDto = new LoginResDto().builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();*/

        return new ResponseEntity<>(new CommonDto(new LoginResDto(accessToken, refreshToken), HttpStatus.OK.value(), "로그인 성공"), HttpStatus.OK);
    }

    // rt를 통한 at 갱신 요청
    @PostMapping("/refresh-at")
    public ResponseEntity<?> generateNewAt(@RequestBody RefreshTokenDto refreshTokenDto) {
//        rt 검증 로직
        Member member = jwtTokenProvider.validateRt(refreshTokenDto.getRefreshToken());

//        at 신규 생성
        String accessToken = jwtTokenProvider.createAtToken(member);
        /*LoginResDto loginResDto = new LoginResDto().builder()
                .accessToken(accessToken)
                .build();*/

        return new ResponseEntity<>(
                new CommonDto(new LoginResDto(accessToken, null), HttpStatus.OK.value(), "at 재발급 성공"), HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<?> memberList() {
        List<MemberResDto> memberList = memberService.findAll();
        return new ResponseEntity<>(new CommonDto(memberList, HttpStatus.OK.value(), "사용자 목록 조회 성공"), HttpStatus.OK);
    }

    @GetMapping("/detail/{inputId}")
    public ResponseEntity<?> memberDetail(@PathVariable Long inputId) {
        MemberResDto dto = memberService.memberDetail(inputId);
        return new ResponseEntity<>(new CommonDto(dto, HttpStatus.OK.value(), "사용자 상세 조회 성공"), HttpStatus.OK);
    }

    @GetMapping("/myInfo")
    public ResponseEntity<?> memberMyInfo(@RequestHeader("X-User-Email") String email) {
        MemberResDto memberResDto = memberService.findMyInfo(email);
        return new ResponseEntity<>(new CommonDto(memberResDto, HttpStatus.OK.value(), "마이페이지 조회 성공"), HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> memberDelete(@RequestHeader("X-User-Email") String email) {
        Long deleteMemberId = memberService.memberDelete(email);
        return new ResponseEntity<>(new CommonDto(deleteMemberId, HttpStatus.OK.value(), "회원탈퇴 성공"), HttpStatus.OK);
    }
}