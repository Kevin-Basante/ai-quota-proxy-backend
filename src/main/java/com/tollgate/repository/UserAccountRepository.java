package com.tollgate.repository;

import com.tollgate.domain.UserAccount;
import com.tollgate.exception.UserNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserAccountRepository {

    private final Map<String, UserAccount> accounts = new ConcurrentHashMap<>();

    public UserAccount save(UserAccount account) {
        accounts.put(account.getId(), account);
        return account;
    }

    public Optional<UserAccount> findById(String id) {
        return Optional.ofNullable(id).map(accounts::get);
    }

    public UserAccount getById(String id) {
        return findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public Collection<UserAccount> findAll() {
        return accounts.values();
    }

    public void deleteAll() {
        accounts.clear();
    }
}
