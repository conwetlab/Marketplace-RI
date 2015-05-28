<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="container">
  <div class="col-sm-10 col-md-4 col-lg-3">
    <t:insertTemplate template="/WEB-INF/views/offerings/header.jsp" />
  </div>
  <div class="col-sm-10 col-md-6 col-lg-7">
    <div class="payment-plan-group">
      <c:forEach var="pricePlan" items="${ offering.pricePlans }">

      <div class="panel panel-default payment-plan">
        <div class="panel-heading text-center">
          <div class="panel-title">${ pricePlan.title }</div>
          <div class="payment-plan-description">${ pricePlan.comment }</div>
        </div>
        <div class="panel-body">
          <hr>
          <c:choose>
          <c:when test="${ not empty pricePlan.priceComponents }">

            <div class="list-group">
              <c:forEach items="${ pricePlan.priceComponents }" var="priceComponent">

              <div
                <c:choose>
                <c:when test="${ f:toLowerCase(priceComponent.unit) == 'single payment' }">
                  class="list-group-item list-group-item-info text-center"
                </c:when>
                <c:otherwise>
                  class="list-group-item list-group-item-warning text-center"
                </c:otherwise>
                </c:choose>
              >
                <p class="value">${ priceComponent.value }<span class="currency">${ priceComponent.currency }</span></p>
                <c:choose>
                <c:when test="${ f:toLowerCase(priceComponent.unit) == 'single payment' }">
                    <p class="units">1 payment</p>
                </c:when>
                <c:otherwise>
                    <p class="units">/ ${ priceComponent.unit }</p>
                </c:otherwise>
                </c:choose>
              </div>

              </c:forEach>
            </div>

          </c:when>
          <c:otherwise>

            <div class="list-group">
              <div class="list-group-item list-group-item-success text-center">
                <p class="value">Free</p>
                <p class="units">right now</p>
              </div>
            </div>

          </c:otherwise>
          </c:choose>
        </div>
      </div>

      </c:forEach>
    </div>
  </div>
</div>