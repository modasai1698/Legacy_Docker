package com.example.web;

import com.example.UserStore;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/** Shows the login form and signs users in. */
@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  static final String SESSION_USER = "user";

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if (req.getSession(false) != null
        && req.getSession(false).getAttribute(SESSION_USER) != null) {
      resp.sendRedirect(req.getContextPath() + "/welcome");
      return;
    }

    String notice = "";
    if (req.getParameter("registered") != null) {
      notice = "<div class=\"notice ok\">Account created! Please sign in.</div>";
    } else if (req.getParameter("loggedout") != null) {
      notice = "<div class=\"notice ok\">You have been signed out.</div>";
    }
    write(resp, HttpServletResponse.SC_OK, page(req.getContextPath(), notice, ""));
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    req.setCharacterEncoding("UTF-8");
    var users = (UserStore) getServletContext().getAttribute(AppInit.USERS);
    String email = req.getParameter("email");

    var user = users.authenticate(email, req.getParameter("password"));
    if (user.isEmpty()) {
      write(resp, HttpServletResponse.SC_UNAUTHORIZED, page(req.getContextPath(),
          "<div class=\"notice bad\">Invalid email or password.</div>",
          email == null ? "" : email.strip()));
      return;
    }

    // New session id after login, so an old (possibly stolen) id is useless.
    HttpSession old = req.getSession(false);
    if (old != null) {
      old.invalidate();
    }
    req.getSession(true).setAttribute(SESSION_USER, user.get());
    resp.sendRedirect(req.getContextPath() + "/welcome");
  }

  private static void write(HttpServletResponse resp, int status, String html)
      throws IOException {
    resp.setStatus(status);
    resp.setContentType("text/html;charset=UTF-8");
    resp.setHeader("Cache-Control", "no-store");
    try (PrintWriter out = resp.getWriter()) {
      out.print(html);
    }
  }

  private static String page(String ctx, String notice, String email) {
    return Pages.form(ctx, "Sign in", """
        <h1>Welcome back</h1>
        <p class="lead">Sign in to your account.</p>
        %s
        <form action="%s/login" method="post">
          <div class="field">
            <label for="email">Email</label>
            <input id="email" name="email" type="email" autocomplete="username"
                   placeholder="asha@example.com" value="%s" required autofocus>
          </div>
          <div class="field">
            <label for="password">Password</label>
            <input id="password" name="password" type="password"
                   autocomplete="current-password" placeholder="Your password" required>
          </div>
          <button type="submit" class="btn">Sign in</button>
        </form>
        <p class="signin">New here? <a href="%s/">Create an account</a></p>
        """.formatted(notice, Html.escape(ctx), Html.escape(email), Html.escape(ctx)));
  }
}
