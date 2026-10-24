package com.example.web;

import com.example.Greeter;
import com.example.UserStore;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/** The page you see after signing in. Redirects to the login page if you are not signed in. */
@WebServlet(name = "WelcomeServlet", urlPatterns = "/welcome")
public class WelcomeServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Greeter GREETER = new Greeter();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String ctx = req.getContextPath();
    HttpSession session = req.getSession(false);
    var user = session == null ? null : (UserStore.User) session.getAttribute(LoginServlet.SESSION_USER);
    if (user == null) {
      resp.sendRedirect(ctx + "/login");
      return;
    }

    resp.setContentType("text/html;charset=UTF-8");
    resp.setHeader("Cache-Control", "no-store");
    try (PrintWriter out = resp.getWriter()) {
      out.print(Pages.result(ctx, "Welcome", """
          <div class="icon ok">&#10003;</div>
          <h1>%s</h1>
          <p class="lead">You are signed in.</p>
          <dl class="summary">
            <dt>Name</dt><dd>%s</dd>
            <dt>Email</dt><dd>%s</dd>
            <dt>Mobile</dt><dd>%s</dd>
          </dl>
          <form action="%s/logout" method="post">
            <button type="submit" class="btn">Sign out</button>
          </form>
          """.formatted(
              Html.escape(GREETER.greet(user.name())),
              Html.escape(user.name()),
              Html.escape(user.email()),
              Html.escape(user.mobile()),
              Html.escape(ctx))));
    }
  }
}
