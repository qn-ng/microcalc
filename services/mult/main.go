package main

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"strconv"
	"encoding/json"
)

const APP_VERSION = "v1"
const APP_BASEPATH = "/api/" + APP_VERSION
const APP_SERVICE = "name: mult, version: " + APP_VERSION

type MultRequest struct {
	Operands [2]FlexInt `json:"operands" binding:"required"`
}

type FlexInt int

func (fi *FlexInt) UnmarshalJSON(b []byte) error {
		if b[0] != '"' {
			return json.Unmarshal(b, (*int)(fi))
		}
		var s string
		if err := json.Unmarshal(b, &s); err != nil {
			return err
		}
		i, err := strconv.Atoi(s)
		if err != nil {
			return err
		}
		*fi = FlexInt(i)
		return nil
}

func main() {
	r := gin.Default()

	
	api := r.Group(APP_BASEPATH)
	{
		api.GET("/status", func(c *gin.Context) {
			c.JSON(200, gin.H{
				"data": "OK",
			})
		})

		api.POST("/mult", func(c *gin.Context) {
			var body MultRequest

			if err := c.ShouldBindJSON(&body); err != nil {
				c.JSON(400, gin.H{
					"error": "Invalid Input",
					"service": APP_SERVICE,
				})
				return
			}

			op1,op2 := body.Operands[0], body.Operands[1]
			fmt.Println("Received", op1, op2)

			c.JSON(200, gin.H{
				"result": op1*op2,
				"operands": body.Operands[0:2],
				"service": APP_SERVICE,
			})
		})
	}

	r.Run() // listen and serve on 0.0.0.0:8080
}