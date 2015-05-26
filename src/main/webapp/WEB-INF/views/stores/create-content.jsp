<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="container">
  <div class="col-sm-10 col-md-4 col-lg-5">
    <h2>Register a new store</h2>
  </div>
  <div class="col-sm-10 col-md-6 col-lg-4">
    <div class="panel panel-default">
      <div class="panel-heading">
        <span class="image-thumbnail">
          <span class="image image-circle image-default-darker">
            <span class="fa fa-building fa-inverse"></span>
          </span>
        </span>
      </div>
      <div class="panel-body">
        <form class="col-md-8 col-md-offset-1" name="store_form" method="post" enctype="multipart/form-data" action="${ pageContext.request.contextPath }/stores/register">
          <div class="form-options">
            <button type="submit" class="btn btn-warning">
              <span class="btn-text">Register</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</div>