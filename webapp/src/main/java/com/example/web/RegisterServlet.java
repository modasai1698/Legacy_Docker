package com.example.web;

import com.example.Registration;
import com.example.UserStore;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Handles the registration form: validates, saves the user, then sends them to the login page.
 */
@WebServlet(name = "RegisterServlet", urlPatterns = "/register")
public class RegisterServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.sendRedirect(req.getContextPath() + "/");
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    req.setCharacterEncoding("UTF-8");
    var users = (UserStore) getServletContext().getAttribute(AppInit.USERS);
    String ctx = req.getContextPath();

    var registration = new Registration(
        req.getParameter("name"),
        req.getParameter("mobile"),
        req.getParameter("email"),
        req.getParameter("password"),
        req.getParameter("passwordRepeat"));

    List<String> errors = registration.validate();
    if (errors.isEmpty() && !users.register(registration)) {
      errors = List.of("That email is already registered. Please sign in instead.");
    }

    if (errors.isEmpty()) {
      resp.sendRedirect(ctx + "/login?registered=1");
      return;
    }

    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    resp.setContentType("text/html;charset=UTF-8");
    resp.setHeader("Cache-Control", "no-store");
    try (PrintWriter out = resp.getWriter()) {
      out.print(errorPage(ctx, errors));
    }
  }

  private String errorPage(String ctx, List<String> errors) {
    var items = new StringBuilder();
    for (String e : errors) {
      items.append("<li>").append(Html.escape(e)).append("</li>");
    }
    return Pages.result(ctx, "Please check your details", """
        <div class="icon bad">!</div>
        <h1>Almost there</h1>
        <p class="lead">Please fix the following and try again:</p>
        <ul class="errors">%s</ul>
        <a class="btn" href="%s/">Go back</a>
        <p class="signin">Already registered? <a href="%s/login">Sign in</a></p>
        """.formatted(items, Html.escape(ctx), Html.escape(ctx)));
  }
}
