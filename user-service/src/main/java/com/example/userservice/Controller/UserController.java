package com.example.userservice.Controller;

import com.example.userservice.domain.CharacterEntity;
import com.example.userservice.service.CharacterService;
import com.example.userservice.service.KakaoService;
import com.example.userservice.dto.FriendDTO;
import com.example.userservice.dto.KakaoFriend;
import com.example.userservice.domain.UserEntity;
import com.example.userservice.dto.KakaoUserDTO;
import com.example.userservice.service.UserService;
import com.example.userservice.dto.KakaoTokenDTO;
import com.example.userservice.dto.TokenSet;
import com.example.userservice.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Transactional
@RestController
@RequiredArgsConstructor
public class UserController {
    private final KakaoService kakaoService;
    private final TokenService tokenService;
    private final UserService userService;
    private final CharacterService characterService;

    //테스트 용 토큰 발급
    @GetMapping("/unauthorization/code")
    public String code(@RequestParam("code") String code){
        KakaoTokenDTO token = kakaoService.getKakaoToken(code);
        return "엑세스 토큰: " + token.getAccess_token() + "\n리프레쉬 토큰: " + token.getRefresh_token();
    }

    /**카카오 로그인(서버 토큰 발급)*/
    @GetMapping("/unauthorization/kakao-login")
    public ResponseEntity<TokenSet> kakaoLogin(@RequestHeader(value = "Authorization") String kakaoToken) {
        kakaoToken = kakaoToken.replace("Bearer ", "");

        KakaoUserDTO kakaoUserDTO = null;

        try {
            kakaoUserDTO = kakaoService.getKakaoUserInfo(kakaoToken);

            if(kakaoUserDTO == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

            Optional<UserEntity> userEntity = userService.findUser(kakaoUserDTO.getId());

            if(!userEntity.isPresent())
                userService.createUser(kakaoUserDTO);

            TokenSet tokenSet = tokenService.createTokenSet(kakaoUserDTO.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(tokenSet);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    /**서버 토큰 재발급*/
    @GetMapping("/unauthorization/refresh")
    public ResponseEntity<Void> tokenRefresh(@RequestHeader(value = "Authorization") String token){
        token = token.replace("Bearer ", "");

        try {
            String newToken = tokenService.renewalToken(token);
            return ResponseEntity.status(HttpStatus.CREATED).header("Authorization", newToken).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }






    /**유저정보 조회*/    //아이디 없이 토큰만 보내서 유저정보 조회하도록
    @GetMapping("/user")
    public ResponseEntity<UserEntity> getUser(@RequestParam("id") Long userId){
        UserEntity user = userService.findUser(userId).get();
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    /**유저정보 업데이트*/
    @PutMapping("/user")
    public HttpStatus firstSetting(@RequestBody UserEntity userEntity){
        try {
            userService.updateUser(userEntity);
            return HttpStatus.OK;
        } catch (SQLException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }




    /**컬렉션 조회*/
    @GetMapping("/collection")
    public ResponseEntity<List<CharacterEntity>> findCollection(@RequestParam("id") Long userId){
        List<CharacterEntity> characterEntityList = null;
        try {
            characterEntityList = characterService.findMyCharacter(userId);
            return ResponseEntity.status(HttpStatus.OK).body(characterEntityList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(characterEntityList);
        }
    }




    /**친구 정보 불러오기*/
    @GetMapping("/kakao-friends")
    public ResponseEntity<List<FriendDTO>> findKakaoFriendsList(@RequestBody String accessToken, @RequestParam("offset") int offset){
        KakaoFriend responseFriends = kakaoService.getKakaoFriends(accessToken, offset);

        if(responseFriends == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

        //카카오 친구 조회에 성공했지만 친구가 없을 때
        else if(responseFriends.getFriends() == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        List<FriendDTO> result =  userService.getFriends(responseFriends);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /**친구의 메인페이지?*/
}
