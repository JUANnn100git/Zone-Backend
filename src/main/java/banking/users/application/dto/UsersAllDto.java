package banking.users.application.dto;

import java.util.List;

public class UsersAllDto {
	
	private List<UserDto> users;
	private long total;
	
	public List<UserDto> getUsers() {
		return users;
	}
	public void setUsers(List<UserDto> users) {
		this.users = users;
	}
	public long getTotal() {
		return total;
	}
	public void setTotal(long countUsers) {
		this.total = countUsers;
	}
	


}
