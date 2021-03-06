package bn.blaszczyk.roseservice.server;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bn.blaszczyk.rosecommon.RoseException;

public interface Endpoint {

	public int get(final String path, final HttpServletRequest request, final HttpServletResponse response) throws RoseException;
	public int post(final String path, final HttpServletRequest request, final HttpServletResponse response) throws RoseException;
	public int put(final String path, final HttpServletRequest request, final HttpServletResponse response) throws RoseException;
	public int delete(final String path, final HttpServletRequest request, final HttpServletResponse response) throws RoseException;
	
	public Map<String,String> status();
	
}
