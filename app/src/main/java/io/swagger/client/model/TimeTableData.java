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

import io.swagger.client.model.CreationDate;
import io.swagger.annotations.*;
import com.google.gson.annotations.SerializedName;

@ApiModel(description = "")
public class TimeTableData {
  
  @SerializedName("CreationDate")
  private CreationDate creationDate = null;

  /**
   **/
  @ApiModelProperty(required = true, value = "")
  public CreationDate getCreationDate() {
    return creationDate;
  }
  public void setCreationDate(CreationDate creationDate) {
    this.creationDate = creationDate;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TimeTableData timeTableData = (TimeTableData) o;
    return (this.creationDate == null ? timeTableData.creationDate == null : this.creationDate.equals(timeTableData.creationDate));
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + (this.creationDate == null ? 0: this.creationDate.hashCode());
    return result;
  }

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class TimeTableData {\n");
    
    sb.append("  creationDate: ").append(creationDate).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
