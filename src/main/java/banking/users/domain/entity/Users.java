package banking.users.domain.entity;

import java.util.List;

public class Users {
	
	private List<User> users;
	private int total;
    
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public List<User> getUsers() {
		return users;
	}
	public void setUsers(List<User> users) {
		this.users = users;
	}
    
}
