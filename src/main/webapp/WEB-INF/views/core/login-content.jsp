<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${ not empty param.out }">
<div class="alert-manager alert-dismissible">
  <c:choose>
  <c:when test="${ param.out == 1 }">
    <div class="alert alert-success">
      <span class="fa fa-check-circle"></span> You've logged out successfully.
    </div>
  </c:when>
  <c:when test="${ param.out == 2 }">
    <div class="alert alert-info">
      <span class="fa fa-check-circle"></span> Your account was deleted successfully.
    </div>
  </c:when>
  <c:when test="${ param.out == 3 }">
    <div class="alert alert-success">
      <span class="fa fa-check-circle"></span> Your password was changed. Please sign in again.
    </div>
  </c:when>
  </c:choose>
</div>
</c:if>

<div class="container">
  <div class="col-sm-10 col-md-6 col-md-offset-2 col-lg-4 col-lg-offset-3">
    <div class="panel panel-default">
      <div class="panel-heading">
        <span class="image-thumbnail">
          <span class="image image-circle image-default-darker">
            <span class="fa fa-user fa-inverse"></span>
          </span>
        </span>
      </div>
      <div class="panel-body">

        <c:if test="${ not empty param.err }">
        <div class="alert alert-danger">
          <span class="fa fa-times-circle"></span> The username and password do not match.
        </div>
        </c:if>

        <form class="col-md-8 col-md-offset-1" name="login_form" method="post" action="<c:url value='j_spring_security_check' />">
          <div class="form-field">
            <label class="field-label">Email or Username</label>
            <input class="field-control" type="text" name="username" />
          </div>
          <div class="form-field">
            <label class="field-label">Password</label>
            <input class="field-control" type="password" name="password" />
          </div>
          <div class="form-options">
            <button class="btn btn-primary" type="submit">
              <span class="btn-text">Sign In</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</div>