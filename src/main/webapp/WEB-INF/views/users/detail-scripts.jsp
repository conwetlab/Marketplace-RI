<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${ user.oauth2 eq false }">
<script src="${ pageContext.request.contextPath }/resources/marketplace/js/views/users/detail.js"></script>
<script>
  <c:choose>
  <c:when test="${ not empty form_data }">
    app.view.accountUpdateForm
    <c:forEach var="field" items="${ form_data }">
    .addInitialValue("${ field.key }", "${ field.value }")
    </c:forEach>
    .addErrorMessage("${ form_error.fieldName }", "${ form_error.fieldError }")
  </c:when>
  <c:otherwise>
    app.view.accountUpdateForm
      .addInitialValue("userName", "${ user.userName }")
      .addInitialValue("displayName", "${ user.displayName }")
      .addInitialValue("email", "${ user.email }")
      .addInitialValue("company", "${ user.company }")
  </c:otherwise>
  </c:choose>
</script>
</c:if>