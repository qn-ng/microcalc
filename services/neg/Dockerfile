FROM microsoft/dotnet:2.1-sdk as builder


COPY app.csproj /app/
WORKDIR /app
RUN dotnet restore

COPY . /app
RUN dotnet build -c Release -o output

FROM microsoft/dotnet:2.1-aspnetcore-runtime-alpine
COPY --from=builder /app/output /app
ENV ASPNETCORE_ENVIRONMENT=production
EXPOSE 80
CMD ["dotnet", "/app/app.dll"]