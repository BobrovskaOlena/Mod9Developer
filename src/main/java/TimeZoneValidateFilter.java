import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.TimeZone;

@WebFilter("/time")
public class TimeZoneValidateFilter extends HttpFilter {
    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        String timeZoneParam = req.getParameter("timezone");

        if (timeZoneParam == null || timeZoneParam.isEmpty()) {
            timeZoneParam = TimeZone.getDefault().getID();
        } else if (timeZoneParam.equals("UTC")) {
            timeZoneParam = TimeZone.getDefault().getID();
        } else if (!isValidTimeZone(timeZoneParam)) {
            resp.setStatus(400);
            resp.getWriter().write("Invalid timezone");
            return;
        }

        req.setAttribute("timezone", timeZoneParam);
        chain.doFilter(req, resp);
    }

    private boolean isValidTimeZone(String timeZoneParam) {
        if (timeZoneParam.length() < 4 || !timeZoneParam.startsWith("UTC")) {
            return false;
        }

        char sign = timeZoneParam.charAt(3);
        String offset = timeZoneParam.substring(4);

        if (sign == '+' || sign == '-') {
            try {
                int offsetHours = Integer.parseInt(offset);
                return offsetHours >= 0 && offsetHours <= 23;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }
}