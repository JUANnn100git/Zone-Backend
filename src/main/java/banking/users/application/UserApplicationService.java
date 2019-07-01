package banking.users.application;

import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import banking.users.application.dto.UserDto;
import banking.users.application.dto.UsersAllDto;
import banking.common.application.Notification;
import banking.common.application.enumeration.RequestBodyType;
import banking.common.infrastructure.security.Hashing;
import banking.common.infrastructure.security.JwtTokenProvider;
import banking.users.application.dto.UserAuthDto;
import banking.users.application.dto.UserClaimDto;
import banking.users.domain.entity.User;
import banking.users.domain.entity.UserClaim;
import banking.users.domain.repository.UserClaimRepository;
import banking.users.domain.repository.UserRepository;

@Service
public class UserApplicationService {
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserClaimRepository userClaimRepository;
	
	@Autowired
	private ModelMapper mapper;
	
	@Autowired
	private JwtTokenProvider jwtTokenProvider;
	
	@Value("${maxPageSize}")
	private int maxPageSize;
    
	public UserDto create(UserDto userDto) {
		Notification notification = this.createValidation(userDto);
        if (notification.hasErrors()) {
            throw new IllegalArgumentException(notification.errorMessage());
        }
		String hashPassword = Hashing.hash(userDto.getPassword());
		userDto.setPassword(hashPassword);
		User user = mapper.map(userDto, User.class);
		user = userRepository.save(user);
		userDto = mapper.map(user, UserDto.class);
        return userDto;
    }
	
	
	private Notification createValidation(UserDto userDto) {
		Notification notification = new Notification();
		if (userDto == null || userDto.getName().equals(RequestBodyType.INVALID.toString())) {
			notification.addError("Invalid JSON data in request body.");
		}
		User user = userRepository.getByName(userDto.getName().trim());
		if (user != null) {
			notification.addError("User name is already registered");
		}
		return notification;
	}
	
	public UserAuthDto validateUser(UserDto userDto) throws Exception {
		UserAuthDto userAuthDto = new UserAuthDto();
		User authUser = null;
		authUser = this.userRepository.getByName(userDto.getName());
		if (authUser == null) {
			return userAuthDto;
		}		
		if (!Hashing.verifyHash(userDto.getPassword(), authUser.getPassword())) {
			return userAuthDto;
		}
		userAuthDto = this.buildUserAuthDto(authUser);
		return userAuthDto;
	}
	
	private List<UserClaimDto> getUserClaims(User authUser) throws Exception {
		List<UserClaim> claims = this.userClaimRepository.findByUserId(authUser.getId());
		return mapper.map(claims, new TypeToken<List<UserClaimDto>>() {}.getType());
	}
	
	private UserAuthDto buildUserAuthDto(User authUser) throws Exception {
		UserAuthDto userAuthDto = new UserAuthDto();
		userAuthDto.setId(authUser.getId());
		userAuthDto.setName(authUser.getName());
		userAuthDto.setAuthenticated(true);
		userAuthDto.setBearerToken(new UUID(0L, 0L).toString());
		List<UserClaimDto> claims = this.getUserClaims(authUser);
		userAuthDto.setClaims(claims);
		userAuthDto.setBearerToken(jwtTokenProvider.buildJwtToken(userAuthDto));
		return userAuthDto;
	}
	
	public UserDto get(long userId) {
		ModelMapper modelMapper = new ModelMapper();
		User user = this.userRepository.getById(userId);
		UserDto userDto = modelMapper.map(user, UserDto.class);
        return userDto;
    }
    
    public List<UserDto> getPaginated(int page, int pageSize) {
		Notification notification = this.getPaginatedValidation(page, pageSize);
        if (notification.hasErrors()) {
            throw new IllegalArgumentException(notification.errorMessage());
        }
		List<User> users = this.userRepository.getPaginated(page, pageSize);
		List<UserDto> usersDto = mapper.map(users, new TypeToken<List<UserDto>>() {}.getType());
        return usersDto;
    }
    
    private Notification getPaginatedValidation(int page, int pageSize) {
		Notification notification = new Notification();
		if (pageSize > maxPageSize) {
			notification.addError("Page size can not be greater than 100");
		}
		return notification;
	}
    
    // Implementando Funciones para el EndPoint de UserClaim para los permisos
    
	public UserClaimDto createClaim(long userId, UserClaimDto userClaimDto) {
		Notification notification = this.createValidationClaim(userClaimDto);
        if (notification.hasErrors()) {
            throw new IllegalArgumentException(notification.errorMessage());
        }
		User user = this.userRepository.getById(userId);
		user = mapper.map(user, User.class);
        UserClaim userClaim = mapper.map(userClaimDto, UserClaim.class);
        userClaim.setUser(user);
        userClaim = userClaimRepository.save(userClaim);
        userClaimDto = mapper.map(userClaim, UserClaimDto.class);
        return userClaimDto;
    }
	
	private Notification createValidationClaim(UserClaimDto userClaimDto) {
		Notification notification = new Notification();
		if (userClaimDto == null ) {
			notification.addError("Invalid JSON data in request body.");
		}
		return notification;
	}
	
	public UserClaimDto getById(long userClaimId) {
		ModelMapper modelMapper = new ModelMapper();
		UserClaim userClaim = this.userClaimRepository.getById(userClaimId);
		
		System.out.println("############## UserClaim ##########  ");
		System.out.println("id: " + userClaim.getId());
		System.out.println("type: " + userClaim.getType());
		System.out.println("value: " + userClaim.getValue());
		System.out.println("user: " + userClaim.getUser());
		
		User user = userClaim.getUser();
		user.setClaims(null);
		UserDto userDto = modelMapper.map(user, UserDto.class);


		UserClaimDto userClaimDto = modelMapper.map(userClaim, UserClaimDto.class);
		userClaimDto.setUserDto(userDto);
        return userClaimDto;
    }
	
	public void deleteClaimById(long userClaimId) {
		ModelMapper modelMapper = new ModelMapper();
		UserClaimDto userClaimDto  = this.getById(userClaimId);
		UserClaim userClaim = modelMapper.map(userClaimDto, UserClaim.class);
		this.userClaimRepository.delete(userClaim);
    }
	
	
    public UsersAllDto getUsersPaginated(int page, int pageSize) {
    	
		Notification notification = this.getPaginatedValidation(page, pageSize);
        if (notification.hasErrors()) {
            throw new IllegalArgumentException(notification.errorMessage());
        }
        
		ModelMapper modelMapper = new ModelMapper();
		UsersAllDto usersAllDto = new UsersAllDto();
		
		List<User> users = this.userRepository.getPaginated(page, pageSize);
		List<UserDto> usersDto = mapper.map(users, new TypeToken<List<UserDto>>() {}.getType());
		
		long countUsers = this.userRepository.count();
		
		usersAllDto.setUsers(usersDto);
		usersAllDto.setTotal(countUsers);

        return usersAllDto;
    }
    
	
}
