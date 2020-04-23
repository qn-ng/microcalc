package main

import (
	"encoding/json"
	"fmt"
	"net/http"
	"strconv"
)

const (
	AppVersion  = "v1"
	AppBasepath = "/api/" + AppVersion
	AppService  = "name: mult, version: " + AppVersion
)

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
	handle("/status", http.MethodGet, func(rw http.ResponseWriter, req *http.Request) {
		rw.WriteHeader(http.StatusOK)
		rw.Write([]byte(`{"data":"OK"}`))
	})

	handle("/mult", http.MethodPost, func(rw http.ResponseWriter, req *http.Request) {
		var body MultRequest

		if err := json.NewDecoder(req.Body).Decode(&body); err != nil {
			rw.WriteHeader(http.StatusBadRequest)
			rw.Write([]byte(fmt.Sprintf(`{"error":"Invalid Input","service":"%s"}`, AppService)))
			return
		}

		op1, op2 := body.Operands[0], body.Operands[1]
		fmt.Println("Received", op1, op2)

		if err := json.NewEncoder(rw).Encode(map[string]interface{}{
			"result":   op1 * op2,
			"operands": body.Operands[0:2],
			"service":  AppService,
		}); err != nil {
			rw.WriteHeader(http.StatusInternalServerError)
		}
	})

	http.ListenAndServe(":8080", http.DefaultServeMux)
}

func prefix(route string) string {
	return fmt.Sprintf("%s%s", AppBasepath, route)
}

func handle(route string, method string, handler func(http.ResponseWriter, *http.Request)) {
	http.HandleFunc(prefix(route), func(rw http.ResponseWriter, req *http.Request) {
		if req.Method != method {
			rw.WriteHeader(http.StatusNotFound)
			return
		}
		rw.Header().Set("Content-Type", "application/json")
		handler(rw, req)
	})
}
