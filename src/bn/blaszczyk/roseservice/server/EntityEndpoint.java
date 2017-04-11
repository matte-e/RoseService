package bn.blaszczyk.roseservice.server;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import bn.blaszczyk.rose.model.Readable;
import bn.blaszczyk.roseservice.RoseException;
import bn.blaszczyk.roseservice.controller.HibernateController;
import bn.blaszczyk.roseservice.tools.TypeManager;

public class EntityEndpoint implements Endpoint {
	
	private final HibernateController controller;

	public EntityEndpoint(final HibernateController controller)
	{
		this.controller = controller;
	}
	
	@Override
	public int get(String path, HttpServletRequest request, HttpServletResponse response) throws RoseException
	{
		final PathOptions options = new PathOptions(path);
		if(!options.isValid())
		{
			return HttpServletResponse.SC_NOT_FOUND;
		}
		try
		{
			if(options.getId()<0)
			{
				// TODO: with query parameters
				final List<RoseDto> dtos = controller
						.getEntites(options.getType())
						.stream()
						.map(RoseDto::new)
						.collect(Collectors.toList());	
				final Gson gson = new Gson();
				response.getWriter().write(gson.toJson(dtos));
				return HttpServletResponse.SC_OK;
			}
			else
			{
				final Readable entity = controller.getEntityById(options.getType(),options.getId());
				if(entity == null)
				{
					response.getWriter().write(options + " not found.");
					return HttpServletResponse.SC_NOT_FOUND;
				}
				else
				{
					final RoseDto dto = new RoseDto(entity);
					final Gson gson = new Gson();
					response.getWriter().write(gson.toJson(dto));
					return HttpServletResponse.SC_OK;
				}
			}
		}
		catch (Exception e) 
		{
			throw new RoseException("error handling entity request", e);
		}
	}
	
	@Override
	public int post(String path, HttpServletRequest request, HttpServletResponse response) throws RoseException
	{
		System.out.println("POST");
		return HttpServletResponse.SC_CREATED;
	}
	
	@Override
	public int put(String path, HttpServletRequest request, HttpServletResponse response) throws RoseException
	{
		System.out.println("PUT");
		return HttpServletResponse.SC_NO_CONTENT;
	}
	
	@Override
	public int delete(String path, HttpServletRequest request, HttpServletResponse response) throws RoseException
	{
		System.out.println("DELETE");
		return HttpServletResponse.SC_NO_CONTENT;
	}
	
	private static class PathOptions
	{
		private final Class<?> type;
		private int id = -1;
		private boolean valid;
		
		public PathOptions(final String path)
		{
			final String[] options = path.split("\\/");
			type = TypeManager.getClass(options[0]);
			valid = type != null;
			if(options.length > 1)
				try
				{
					id = Integer.parseInt(options[1].trim());
				}
				catch (NumberFormatException e) 
				{
					valid = false;
				}
		}

		public int getId()
		{
			return id;
		}

		public boolean isValid()
		{
			return valid;
		}

		public Class<?> getType()
		{
			return type;
		}
		
		@Override
		public String toString()
		{
			if(!valid)
				return "Invalid Path Options";
			return type.getSimpleName() + " id=" + id;
		}
		
		
	}
	
}
