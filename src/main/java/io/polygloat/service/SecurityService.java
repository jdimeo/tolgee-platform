package io.polygloat.service;

import io.polygloat.constants.ApiScope;
import io.polygloat.exceptions.PermissionException;
import io.polygloat.model.ApiKey;
import io.polygloat.model.Permission;
import io.polygloat.model.Repository;
import io.polygloat.model.UserAccount;
import io.polygloat.security.AuthenticationFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
public class SecurityService {
    private final AuthenticationFacade authenticationFacade;
    private final PermissionService permissionService;
    private final ApiKeyService apiKeyService;

    @Autowired
    public SecurityService(AuthenticationFacade authenticationFacade, PermissionService permissionService, ApiKeyService apiKeyService) {
        this.authenticationFacade = authenticationFacade;
        this.permissionService = permissionService;
        this.apiKeyService = apiKeyService;
    }

    @Transactional
    public void grantFullAccessToRepo(Repository repository) {
        permissionService.grantFullAccessToRepo(getActiveUser(), repository);
    }

    private Optional<Permission> getRepositoryPermission(Long repositoryId) {
        return permissionService.getRepositoryPermission(repositoryId, getActiveUser());
    }

    public Permission getAnyRepositoryPermission(Long repositoryId) {
        Optional<Permission> repositoryPermission = getRepositoryPermission(repositoryId);
        if (repositoryPermission.isEmpty()) {
            throw new PermissionException();
        }

        return repositoryPermission.get();
    }


    public Permission checkRepositoryPermission(Long repositoryId, Permission.RepositoryPermissionType requiredPermission) {
        Permission usersPermission = getAnyRepositoryPermission(repositoryId);
        if (requiredPermission.getPower() > usersPermission.getType().getPower()) {
            throw new PermissionException();
        }
        return usersPermission;
    }


    public void checkApiKeyScopes(Set<ApiScope> scopes, Repository repository) {
        if (!apiKeyService.getAvailableScopes(getActiveUser(), repository).containsAll(scopes)) {
            throw new PermissionException();
        }
    }

    public void checkApiKeyScopes(Set<ApiScope> scopes, ApiKey apiKey) {
        checkApiKeyScopes(scopes, apiKey.getRepository()); // checks if user's has permissions to use api key with api key's permissions
        if (!apiKey.getScopes().containsAll(scopes)) {
            throw new PermissionException();
        }
    }

    private UserAccount getActiveUser() {
        return this.authenticationFacade.getUserAccount();
    }

}
