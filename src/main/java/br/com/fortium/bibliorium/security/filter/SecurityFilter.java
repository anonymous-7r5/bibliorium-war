package br.com.fortium.bibliorium.security.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.fortium.bibliorium.managedbean.login.LoginBean;
import br.com.fortium.bibliorium.persistence.entity.Usuario;
import br.com.fortium.bibliorium.persistence.enumeration.TipoUsuario;

public class SecurityFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		HttpServletResponse httpResponse = (HttpServletResponse)response;
		
		String upperUrl = httpRequest.getRequestURL().toString().toUpperCase();
		
		TipoUsuario tipo = (TipoUsuario)httpRequest.getSession(true).getAttribute(Usuario.AUTENTICADO);
		
		if(!isFolderNamesValid(httpRequest)){
			redirectToLoginWithError(httpRequest, httpResponse, "Acesso negado: Nice try... nada de tentar burlar minha seguran�a.");
			return;
		}
		
		if(tipo == null){
			redirectToLoginWithError(httpRequest, httpResponse, "Acesso negado: Voc� n�o est� logado no sistema ou a sua sess�o expirou.");
			return;
		}else{
			
			if(upperUrl.contains("PAGES/LEITOR") && (tipo == TipoUsuario.PROFESSOR || tipo == TipoUsuario.ALUNO)){
				doFilter(httpRequest, response, chain);
			}else if(upperUrl.contains("PAGES/BIBLIOTECARIO") && (tipo == TipoUsuario.BIBLIOTECARIO)){
				doFilter(httpRequest, response, chain);
			}else{
				redirectToLoginWithError(httpRequest, httpResponse, "Acesso negado: Voc� n�o tem permiss�o para acessar esta funcionalidade.");
				return;
			}
		}
	}

	@Override
	public void destroy() {}
	
	private boolean isFolderNamesValid(HttpServletRequest request){
		String requestOriginal = request.getRequestURL().toString().split("://")[1];
		String folderNames = requestOriginal.substring(requestOriginal.indexOf("/"), requestOriginal.lastIndexOf("/"));
		
		String lowerFolderNames = folderNames.toLowerCase();
		
		return folderNames.equals(lowerFolderNames);
	}

	private void redirectToLoginWithError(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String errorMessage) throws IOException{
		httpRequest.getSession().setAttribute(LoginBean.MENSAGEM_ACESSO_NEGADO, errorMessage);
		httpResponse.sendRedirect("/bibliorium/login.xhtml");
	}
}
