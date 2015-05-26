<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script src="${ pageContext.request.contextPath }/resources/marketplace/js/views/users/credentials.js"></script>
<script>
  <c:if test="${ not empty form_error }">
    app.view.passwordUpdateForm
      .addErrorMessage("${ form_error.fieldName }", "${ form_error.fieldError }")
  </c:if>
</script>