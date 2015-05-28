<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script src="${ pageContext.request.contextPath }/resources/marketplace/js/views/registration.js"></script>
<script>
  <c:if test="${ not empty form_data }">
  app.view.registrationForm
  <c:forEach var="field" items="${ form_data }">
    .addInitialValue("${ field.key }", "${ field.value }")
  </c:forEach>
    .addErrorMessage("${ form_error.fieldName }", "${ form_error.fieldError }")
  </c:if>
</script>