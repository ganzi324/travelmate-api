package com.ganzi.travelmate.auth;

import com.ganzi.travelmate.auth.dto.OAuthAttributes;
import com.ganzi.travelmate.auth.dto.PrincipalInfo;
import com.ganzi.travelmate.user.application.command.AddUserCommand;
import com.ganzi.travelmate.user.application.port.in.AddUserUseCase;
import com.ganzi.travelmate.user.application.port.in.GetUserQuery;
import com.ganzi.travelmate.user.domain.User;
import com.ganzi.travelmate.user.domain.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final GetUserQuery getUserQuery;
    private final AddUserUseCase addUserUseCase;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest
                .getClientRegistration()
                .getRegistrationId();
        String userNameAttributeName = userRequest
                .getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes
                .of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        User user = saveOrUpdate(attributes);

        return new PrincipalInfo(
                Collections.singleton(new SimpleGrantedAuthority(user.getUserRole().getCode())),
                attributes.getAttributes(),
                userNameAttributeName,
                user
        );
    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        String userKey = attributes.registrationId() + "_" + attributes.providerId();

        Optional<User> currentUser = getUserQuery.getUserByUserKey(userKey);
        if (currentUser.isPresent()) {
            return currentUser.get();
        }

        AddUserCommand command = AddUserCommand.createSnsUser(
                attributes.providerId(),
                attributes.name(),
                attributes.email(),
                attributes.picture(),
                UserType.from(attributes.registrationId())
        );
        return addUserUseCase.addUser(command);
    }
}
