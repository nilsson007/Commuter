/**
 * Reseplaneraren
 * Provides access to Västtrafik journey planner
 *
 * OpenAPI spec version: 1.10.1
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package io.swagger.client.model;

import io.swagger.annotations.*;
import com.google.gson.annotations.SerializedName;

@ApiModel(description = "")
public class GeometryRef {
  
  @SerializedName("ref")
  private String ref = null;

  /**
   * Contains a URL to call the REST interface for geometry
   **/
  @ApiModelProperty(required = true, value = "Contains a URL to call the REST interface for geometry")
  public String getRef() {
    return ref;
  }
  public void setRef(String ref) {
    this.ref = ref;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GeometryRef geometryRef = (GeometryRef) o;
    return (this.ref == null ? geometryRef.ref == null : this.ref.equals(geometryRef.ref));
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + (this.ref == null ? 0: this.ref.hashCode());
    return result;
  }

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class GeometryRef {\n");
    
    sb.append("  ref: ").append(ref).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
