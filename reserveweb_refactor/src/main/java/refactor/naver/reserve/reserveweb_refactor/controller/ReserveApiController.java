package refactor.naver.reserve.reserveweb_refactor.controller;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import refactor.naver.reserve.reserveweb_refactor.dto.*;
import refactor.naver.reserve.reserveweb_refactor.entity.User;
import refactor.naver.reserve.reserveweb_refactor.jwt.JwtFilter;
import refactor.naver.reserve.reserveweb_refactor.jwt.TokenProvider;
import refactor.naver.reserve.reserveweb_refactor.service.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(path = "/api")
public class ReserveApiController {

    private final CategoryService categoryService;
    private final PromotionService promotionService;
    private final ProductService productService;
    private final ReservationService reservationService;
    private final UserService userService;
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RedisTemplate redisTemplate;

    public ReserveApiController(CategoryService categoryService, PromotionService promotionService, ProductService productService, ReservationService reservationService, UserService userService,
                                TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder, RedisTemplate redisTemplate) {
        this.categoryService = categoryService;
        this.promotionService = promotionService;
        this.productService = productService;
        this.reservationService = reservationService;
        this.userService = userService;
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("categories")
    public ResponseEntity<CategoryResponseDto> getCategories() {
        ResponseEntity<CategoryResponseDto> response = null;

        try {
            response = new ResponseEntity<>(categoryService.findCategory(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @GetMapping("promotions")
    public ResponseEntity<PromotionResponseDto> getPromotions() {
        ResponseEntity<PromotionResponseDto> response = null;
        try {
            response = new ResponseEntity<>(promotionService.getPromotion(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @GetMapping("products")
    public ResponseEntity<ProductResponseDto> getProducts(@RequestParam int categoryId, @RequestParam(defaultValue = "0") int start) {
        ResponseEntity<ProductResponseDto> response = null;
        try {
            MoreViewRequestDto moreViewRequestDto = new MoreViewRequestDto(start);
            response = new ResponseEntity<>(productService.getProduct(categoryId, moreViewRequestDto), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            response = new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @GetMapping("products/{displayInfoId}")
    public ResponseEntity<DisplayInfoResponseDto> getProductsDetail(@PathVariable("displayInfoId") int displayInfoId) {
        ResponseEntity<DisplayInfoResponseDto> response = null;
        try {
            response = new ResponseEntity<>(productService.getProductDetail(displayInfoId), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @GetMapping("reserve/{displayInfoId}")
    public ResponseEntity<ReservationResponseDto> getReservePageInfo(@PathVariable("displayInfoId") int displayInfoId) {
        ResponseEntity<ReservationResponseDto> response = null;
        try {
            response = new ResponseEntity<>(reservationService.getReservePageInfo(displayInfoId), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @PostMapping("reserve")
    public ResponseEntity<String> createReservation(@RequestBody ReservationRequestDto reservationRequestDto) {
        ResponseEntity<String> response = null;
        try {
            reservationService.createReservation(reservationRequestDto);
            response = new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            response = new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @PostMapping("signup")
    public ResponseEntity<String> signup(@RequestBody UserRequestDto.Signup signup) {
        ResponseEntity<String> response = null;
        try {
            userService.signup(signup);
            response = new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @PostMapping("doLogin")
    public ResponseEntity<UserResponseDto> doLogin(@RequestBody UserRequestDto.Login login) throws Exception {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword());

        // authenticate ???????????? ???????????? CustomUserDetailsServiceImpl => loadUserByUsername ???????????? ??????????????? ????????????.
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserResponseDto userResponseDto = tokenProvider.createToken(authentication);
        User user = userService.getUser(login.getEmail(), login.getPassword());

        userResponseDto.setUserId(user.getUserId());
        userResponseDto.setEmail(user.getEmail());

        redisTemplate.opsForValue()
                .set("RT:" + authentication.getName(), userResponseDto.getRefreshToken(), userResponseDto.getRefreshTokenValidityTime(), TimeUnit.MILLISECONDS);
        return new ResponseEntity(userResponseDto, HttpStatus.OK);
    }

    @PostMapping("reIssue")
    public ResponseEntity<UserResponseDto> reIssueToken(@RequestBody UserRequestDto.ReIssue reIssue) {
        // 1. Refresh Token ??????
        if (!tokenProvider.validateToken(reIssue.getRefreshToken())) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        // 2. Access Token?????? User email??? ???????????????.
        Authentication authentication = tokenProvider.getAuthentication(reIssue.getAccessToken());

        // 3. Redis?????? User email??? ???????????? ????????? Refresh Token?????? ???????????????.
        String refreshToken = redisTemplate.opsForValue().get("RT:" + authentication.getName()).toString();

        if (!StringUtils.hasText(refreshToken)) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        // ?????? refreshToken??? redis??? ???????????? ?????? refreshToken??? ???????????? ?????? ??????
        if (!refreshToken.equals(reIssue.getRefreshToken())) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        // 4. ????????? ?????? ??????
        UserResponseDto userResponseDto = tokenProvider.createToken(authentication);

        // 5. RefreshToken redis ????????????
        redisTemplate.opsForValue()
                .set("RT:" + authentication.getName(), userResponseDto.getRefreshToken(), userResponseDto.getRefreshTokenValidityTime(), TimeUnit.MILLISECONDS);

        return new ResponseEntity<>(userResponseDto, HttpStatus.OK);
    }

    @PostMapping("doLogout")
    public ResponseEntity<Void> doLogout(@RequestBody UserRequestDto.Logout logout) {
        // 1. Access Token ??????
        if (!tokenProvider.validateToken(logout.getAccessToken())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // 2. Access Token?????? User email??? ???????????????.
        Authentication authentication = tokenProvider.getAuthentication(logout.getAccessToken());

        // 3. Redis?????? ?????? User email??? ????????? Refresh Token??? ????????? ?????????, ???????????? ???????????????.
        if (redisTemplate.opsForValue().get("RT:" + authentication.getName()) != null) {
            redisTemplate.delete("RT:" + authentication.getName());
        }

        // 4. ?????? Access Token ???????????? ????????? ?????? BlackList??? ????????????
        Long expiration = tokenProvider.getExpiration(logout.getAccessToken());
        redisTemplate.opsForValue().set(logout.getAccessToken(), "logout", expiration, TimeUnit.MILLISECONDS);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
