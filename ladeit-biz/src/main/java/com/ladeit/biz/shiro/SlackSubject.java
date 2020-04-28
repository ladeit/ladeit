package com.ladeit.biz.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.ExecutionException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @program: ladeit
 * @description: SlackSubject
 * @author: falcomlife
 * @create: 2020/04/17
 * @version: 1.0.0
 */
public class SlackSubject implements Subject {
	private Object principal;

	@Override
	public Object getPrincipal() {
		return this.principal;
	}

	public void setPrincipal(Object obj) {
		this.principal = obj;
	}

	@Override
	public PrincipalCollection getPrincipals() {
		return null;
	}

	@Override
	public boolean isPermitted(String permission) {
		return false;
	}

	@Override
	public boolean isPermitted(Permission permission) {
		return false;
	}

	@Override
	public boolean[] isPermitted(String... permissions) {
		return new boolean[0];
	}

	@Override
	public boolean[] isPermitted(List<Permission> permissions) {
		return new boolean[0];
	}

	@Override
	public boolean isPermittedAll(String... permissions) {
		return false;
	}

	@Override
	public boolean isPermittedAll(Collection<Permission> permissions) {
		return false;
	}

	@Override
	public void checkPermission(String permission) throws AuthorizationException {

	}

	@Override
	public void checkPermission(Permission permission) throws AuthorizationException {

	}

	@Override
	public void checkPermissions(String... permissions) throws AuthorizationException {

	}

	@Override
	public void checkPermissions(Collection<Permission> permissions) throws AuthorizationException {

	}

	@Override
	public boolean hasRole(String roleIdentifier) {
		return false;
	}

	@Override
	public boolean[] hasRoles(List<String> roleIdentifiers) {
		return new boolean[0];
	}

	@Override
	public boolean hasAllRoles(Collection<String> roleIdentifiers) {
		return false;
	}

	@Override
	public void checkRole(String roleIdentifier) throws AuthorizationException {

	}

	@Override
	public void checkRoles(Collection<String> roleIdentifiers) throws AuthorizationException {

	}

	@Override
	public void checkRoles(String... roleIdentifiers) throws AuthorizationException {

	}

	@Override
	public void login(AuthenticationToken token) throws AuthenticationException {

	}

	@Override
	public boolean isAuthenticated() {
		return false;
	}

	@Override
	public boolean isRemembered() {
		return false;
	}

	@Override
	public Session getSession() {
		return null;
	}

	@Override
	public Session getSession(boolean create) {
		return null;
	}

	@Override
	public void logout() {

	}

	@Override
	public <V> V execute(Callable<V> callable) throws ExecutionException {
		return null;
	}

	@Override
	public void execute(Runnable runnable) {

	}

	@Override
	public <V> Callable<V> associateWith(Callable<V> callable) {
		return null;
	}

	@Override
	public Runnable associateWith(Runnable runnable) {
		return null;
	}

	@Override
	public void runAs(PrincipalCollection principals) throws NullPointerException, IllegalStateException {

	}

	@Override
	public boolean isRunAs() {
		return false;
	}

	@Override
	public PrincipalCollection getPreviousPrincipals() {
		return null;
	}

	@Override
	public PrincipalCollection releaseRunAs() {
		return null;
	}
}
