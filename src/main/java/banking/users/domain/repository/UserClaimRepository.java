package banking.users.domain.repository;

import java.util.List;

import banking.users.domain.entity.UserClaim;

public interface UserClaimRepository {
	public UserClaim getById(long userClaimId);
	public List<UserClaim> findByUserId(Long userId) throws Exception;
	public UserClaim save(UserClaim userClaim);
	public void delete(UserClaim userClaim);
	
}
