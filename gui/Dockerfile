FROM node:latest as web-build

#Setting the working directory as /app
WORKDIR /gui

# add `/app/node_modules/.bin` to $PATH
ENV PATH /gui/node_modules/.bin:$PATH

#Copying package.json to Docker Image
COPY package*.json ./

#Installing all dependencies.
RUN npm install --silent

COPY . ./

EXPOSE 3000
# Running the dev server.
CMD ["npm", "start"]