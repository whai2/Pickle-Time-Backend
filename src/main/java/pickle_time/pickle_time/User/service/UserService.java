package pickle_time.pickle_time.User.service;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pickle_time.pickle_time.Scrap.model.Scrap;
import pickle_time.pickle_time.Scrap.repository.ScrapRepository;
import pickle_time.pickle_time.User.Role;
import pickle_time.pickle_time.global.auth.PrincipalDetails;
import pickle_time.pickle_time.global.auth.UserDetailService;

import pickle_time.pickle_time.User.Repository.UserRepository;
import pickle_time.pickle_time.User.dto.response.UserProfileResponse;
import pickle_time.pickle_time.User.model.Users;
import pickle_time.pickle_time.User.dto.request.UserJoinRequest;
import pickle_time.pickle_time.User.dto.request.UserLoginRequest;
import pickle_time.pickle_time.User.dto.request.UserUpdateRequest;
import pickle_time.pickle_time.global.auth.jwt.TokenProvider;
import pickle_time.pickle_time.global.auth.oauth.ProviderType;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor // final 붙으면 autowire 됨
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ScrapRepository scrapRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserDetailService userDetailService;
    private final TokenProvider tokenProvider;

    /**
     * email 중복 체크
     * 회원가입 기능 구현 시 사용
     * 중복되면 true return
     */
    public boolean checkEmailDuplicate(String email, ProviderType providerType) {
        System.out.println(email);
        System.out.println(providerType);
        boolean result = userRepository.existsByEmailAndProviderType(email, providerType);

        System.out.println(result);

        return result;
    }

    /**
     * nickname 중복 체크
     * 회원가입 기능 구현 시 사용
     * 중복되면 true return
     */
    public boolean checkNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public Users join(UserJoinRequest request) {
        if (checkEmailDuplicate(request.email(), ProviderType.GENERAL)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
//        if (checkNicknameDuplicate(request.nickname())) {
//            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
//        }
        if (!request.password().equals(request.checkPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String encodedPassword = bCryptPasswordEncoder.encode(request.password());

        Users users = Users.builder()
                .password(encodedPassword)
//                .nickname(request.nickname())
                .email(request.email())
                // .company(request.company())
                // .imageUrl(request.imageUrl())
                .role(Role.ROLE_USER)
                .providerType(ProviderType.GENERAL)
                .build();

        return userRepository.save(users);
    }

    public String login(UserLoginRequest userLoginRequest) {
        Users users = userRepository.findByEmailAndProviderType(userLoginRequest.email(), ProviderType.GENERAL)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일이 존재하지 않습니다."));

        if (!bCryptPasswordEncoder.matches(userLoginRequest.password(), users.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        PrincipalDetails principalDetails = (PrincipalDetails) userDetailService.loadUserByUsername(users.getEmail());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());

    return tokenProvider.generateToken(authentication);


    }

    public Optional<Users> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<Scrap> findScrapsById(Long userId) {
        return scrapRepository.findByUserId(userId);
    }


    public UserProfileResponse updateUsers(Long id, UserUpdateRequest userUpdateRequest) {
        Users users = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        System.out.println(userUpdateRequest.nickname());
        users.update(userUpdateRequest.nickname(), userUpdateRequest.company(), userUpdateRequest.imageUrl());


        userRepository.save(users);

        return new UserProfileResponse(users.getNickname(), users.getEmail(), users.getCompany(), users.getImageUrl());
    }

    public void deleteUsers(Long id) {
        Users users = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
        userRepository.delete(users);
    }
}
