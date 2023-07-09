import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@WebServlet(value = "/time")
public class TimeServlet extends HttpServlet {
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        super.init();
        templateEngine = new TemplateEngine();

        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix("C:/Users/HP/Documents/GitHub/Mod9Developer/src/main/webapp/WEB-INF/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        templateEngine.setTemplateResolver(resolver);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        request.getLocale();
        //response.setHeader("Refresh", "1");
        Context context = new Context();
        String timeZoneParam = request.getParameter("timezone");
        ZoneId zoneId;

        if (timeZoneParam == null || timeZoneParam.isEmpty()) {
            zoneId = parseTimeZone(getLastTimezoneFromCookie(request)).orElse(ZoneId.systemDefault());
        } else {
            zoneId = parseTimeZone(timeZoneParam).orElse(ZoneId.of("UTC"));
        }

        Cookie cookie = new Cookie("lastTimezone", zoneId.toString());
        cookie.setMaxAge(60 * 60 * 24 * 365);
        response.addCookie(cookie);

        ZonedDateTime currentTime = ZonedDateTime.now(zoneId);
        ZoneOffset currentOffset = currentTime.getOffset();
        String formattedTime = currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));

        context.setVariable("formattedTime", formattedTime);
        context.setVariable("currentOffset", formatOffset(currentOffset));
        context.setVariable("currentZone", zoneId.toString());

        String output = templateEngine.process("time_template", context);
        out.write(output);
        out.close();
    }

    private String getLastTimezoneFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("lastTimezone")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private Optional<ZoneId> parseTimeZone(String timeZoneParam) {
        if (timeZoneParam == null || timeZoneParam.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(ZoneId.of(timeZoneParam));
        } catch (Exception e) {
            try {
                int offsetHours = Integer.parseInt(timeZoneParam.substring(4));
                return Optional.of(ZoneOffset.ofHours(offsetHours).normalized());
            } catch (Exception ex) {
                return Optional.empty();
            }
        }
    }

    private String formatOffset(ZoneOffset offset) {
        int totalSeconds = offset.getTotalSeconds();
        int hours = totalSeconds / 3600;
        int minutes = Math.abs(totalSeconds % 3600) / 60;

        return String.format("%+03d:%02d", hours, minutes);
    }
}